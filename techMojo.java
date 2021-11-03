import javax.servlet.Filter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/*! Using Google Guvava module for caching */
import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Component
public class requestThrottleFilter implements Filter {

    private int MAX_REQUESTS_PER_HOUR = 10000; // let's take max request per hour = 10k/hour

    private LoadingCache<String, Integer> requestCountsPerIpAddress;

    public requestThrottleFilter(){
        super();
        requestCountsPerIpAddress = CacheBuilder.newBuilder()
                .maximumSize(100)                           // Took 100 as max entries for just this case
                .expireAfterWrite(1, TimeUnit.HOURS)        // Problem statements request(s) time period of 1Hour // Cache will expire after 1 hour
                .build(new CacheLoader<String, Integer>() { 
            public Integer load(String key) {
                return 0;
            }
        });
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        String clientIpAddress = getClientIP((HttpServletRequest) servletRequest);
        if(isMaximumRequestsPerSecondExceeded(clientIpAddress)){
          httpServletResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
          httpServletResponse.getWriter().write("Too many requests");
          return;
         }

        filterChain.doFilter(servletRequest, servletResponse);
    }
    
    /*
     * return Code     | Meaning
     * 0               | Success
     * 1               | Too many requests
    */
    private int isMaximumRequestsPerSecondExceeded(String clientIpAddress){
        int requests = 0;
        try {
            requests = requestCountsPerIpAddress.get(clientIpAddress);
            if(requests > MAX_REQUESTS_PER_HOUR){
                requestCountsPerIpAddress.put(clientIpAddress, requests);
                return 0;
             }
        } catch (ExecutionException e) {
            requests = 0;
        }
        requests++;
        requestCountsPerIpAddress.put(clientIpAddress, requests);
        return 1;
    }

    public String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null){
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0]; 
    }

    @Override
    public void destroy() {

    }
}