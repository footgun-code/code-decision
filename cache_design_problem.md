**Problem Statement**: Design an In-Memory LRU Cache
You are building a caching library that will be used by multiple backend services.
Design and implement an in-memory cache supporting the following requirements.
Functional Requirements
put(key, value)
Insert a new key-value pair.
Update value if key already exists.
get(key)
Return value associated with key.
Return null if the key doesn't exist.
Cache has a fixed capacity N.
When cache reaches capacity:
Evict the Least Recently Used (LRU) entry.
Any successful get() or put() should mark an entry as recently used.

Performance Requirements
Operation
Expected Complexity
get()
O(1)
put()
O(1)
remove()
O(1)



Non-Functional Requirements
Cache should support millions of operations.
Memory overhead should be minimized.
Design should be extensible for future eviction policies.
API should be thread-safe.

Follow-up Requirements

Phase 2
Add TTL Support
put(key, value, ttl)
Requirements:
Expired entries should not be returned.
Expired entries should eventually be removed.



Phase 3
Thread Safety
Multiple threads may perform:
get()
put()
remove()


Phase 4
Metrics
Expose:
hitCount()
missCount()
evictionCount()



**Design Discussion and class diagram
**

<img width="1880" height="3531" alt="LRU_cache" src="https://github.com/user-attachments/assets/e4bb2802-5d9b-42c5-98d7-a085628832c6" />







