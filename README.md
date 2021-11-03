# TechMojo
Rate Limiting Means ability to make sure your API can be used only for X number of times in a
particular time period     
As an API provider we have decided to rate limit the callers with X Number calls per Hour per
each tenant        
If the caller has reached the Limit, we have to return an error code stating same for remaining
time of that hour,          
Once the block period is completed user should be able to access the API         
Note : For simplicity let’s assume there is only one API exposed to the tenant and rate Limit is
calculated at each clock hour   
      
Tenant : This is a third party company registered for the API calls
