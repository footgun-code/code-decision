
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;


interface EvictionPolicy<K> {
     void onAccess(K key); // called on get() hit
     void onInsert(K key);  // called on put() new key
     void onRemove(K key);  // called on explicit remove()
     K evict();  // returns the key to evict; cache removes it
    
}


 class LRUEvictionPolicy<K> implements EvictionPolicy<K>  {

    private static class Node<T> {
        T key;
        Node<T> prev, next;
        Node(){}
        Node(T key){
            this.key=key;
        }
    }
    private final Node<K> head = new Node<>();
    private final Node<K> tail = new Node<>();
    private final Map<K, Node<K>> map= new HashMap<>();

    LRUEvictionPolicy(){
        this.head.next=this.tail;
        this.tail.prev=this.head;
    }


    private void unlink(Node<K> node){
        node.prev.next=node.next; 
        node.next.prev=node.prev;
    }
    private void addToTail(Node<K> node){
        node.prev=this.tail.prev;
        this.tail.prev.next=node;
        node.next= this.tail;
        this.tail.prev=node;
    }

    @Override
    public void onAccess(K key) {
        Node<K> node = this.map.get(key);
        if(node==null) return;
        this.unlink(node);
        this.addToTail(node);// MRU 
    }

    @Override
    public void onInsert(K key) {
        Node<K> node = new Node<>(key);
        this.map.put(key, node); // Key: node
        this.addToTail(node); // DLL 
    }

    @Override
    public void onRemove(K key) {
        Node<K> node = this.map.remove(key);
        if(node==null) return;
        this.unlink(node); // DLL
    }

    @Override
    public K evict() {
        if(this.head.next==this.tail) throw new IllegalStateException("Policy is empty");
        K key = this.head.next.key;// LRU
        this.unlink(this.head.next);
        this.map.remove(key);
        return key;
    }
}

public class Cache<K,V> {

    private static class Entry<T> {
        T value; 
        Instant expireAt;
        Entry(T value, Duration duration){
            this.value=value;
            this.expireAt = Instant.now().plus(duration);
        }
    }

    private final ReentrantLock lock;
    private final Map<K,Entry<V>> cacheMap;
    private final EvictionPolicy<K> policy;
    private final int capacity;
    private static final int DEFAULT_DURATION_DAYS=1000;
    private long hit;
    private long miss;
    private long evict;

    public Cache(int capacity, EvictionPolicy<K> policy){
        if(capacity<=0){
            throw new IllegalArgumentException("Capacity should be positive integer");
        }
        this.capacity=capacity;
        this.cacheMap= new HashMap<>((int)(this.capacity / 0.75) + 1); // actual capacity = 75% of initials means , hashmap rehashmap when 75% full
        this.lock = new ReentrantLock();
        this.policy=policy;
    }

    public Cache(int capacity){
        this(capacity, new LRUEvictionPolicy<>());
    }

    public void put(K key, V value, Duration duration){
        this.lock.lock();
        try{
            Entry<V> node = this.cacheMap.get(key);
            if(node==null){
                node = new Entry<>(value, duration);
                this.cacheMap.put(key, node);
                this.policy.onInsert(key);
            }
        else{
            node.value=value;
            node.expireAt=Instant.now().plus(duration);
            this.policy.onAccess(key);
        }
        
        if(this.cacheMap.size()>this.capacity){
            K deletedKey = this.policy.evict();
            this.cacheMap.remove(deletedKey);
            this.evict++;// increment eviction count
        }
        }
        finally{
            this.lock.unlock();
        }

    }

    public void put(K key, V value){
        this.put(key, value, Duration.ofDays(DEFAULT_DURATION_DAYS));
    }

    public V get(K key){
        this.lock.lock();
        try{
            Entry<V> node = this.cacheMap.get(key);
            if(node==null){
                this.miss++;
                return null;
            }
            if(node.expireAt.isBefore(Instant.now())){
                // as of now Used Lazy cleanup but for production background clean prefered for clean memory periodically
                this.cacheMap.remove(key);
                this.policy.onRemove(key);
                this.miss++;
                return null;
            }
        this.policy.onAccess(key);
        this.hit++;
        return node.value;
        }
        finally{
            this.lock.unlock();
        }
        
    }

    public void remove(K key){
        this.lock.lock();
        try{
            Entry<V> node = this.cacheMap.remove(key);
            if(node!=null){
                this.policy.onRemove(key);
            }
        }
        finally{
            this.lock.unlock();
        }
    }

    public long getHitCount(){
        this.lock.lock();
        try{
            return this.hit;
        }
        finally{
            this.lock.unlock();
        }
    }

    public long getMissCount(){
        this.lock.lock();
        try{
            return this.miss;
        }
        finally{
            this.lock.unlock();
        }
    }

    public long getEvictionCount(){
        this.lock.lock();
        try{
            return this.evict;
        }
        finally{
            this.lock.unlock();
        }
    }
    
}
