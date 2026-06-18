public class MyHashMap<K,V> {

    private static class Entry<K,V> {
        K key;
        V value;
        int hash;
        Entry<K,V> next;
        Entry(K key, V value, int hash){
            this.key=key;
            this.value=value;
            this.hash=hash;
        }
    }
    private static final int DEFAULT_CAPACITY = 16;
    private static final float DEFAULT_LOADFACTOR = 0.75f;
    private Entry<K,V>[] bucket;
    private int size;
    private int capacity;
    private final float loadFactor;

    public MyHashMap(){
        this(DEFAULT_CAPACITY, DEFAULT_LOADFACTOR);
    }
    
    public MyHashMap(int capacity, float loadFactor){
        this.capacity = nextPowerOfTwo(capacity);
        this.loadFactor = loadFactor;
        this.bucket = new Entry[this.capacity];
    }


    private int nextPowerOfTwo(int n){
        if(n<=1) return 1;
        int p=1;
        while(p<n) p <<=1;
        return p;
    }

    private int hash(Object key){
        if(key == null) return 0;
        int h = key.hashCode();
        return h ^ (h >>> 16); // spread: upper bits influence lower bits
    }
    
    private int bucketIndex(int h){
        return h & (this.capacity -1);
    }

    private boolean keyEquals(K key1, K key2){
        return key1==key2 || (key1 !=null && key1.equals(key2));
    }

    public void put(K key, V value){
        int h = this.hash(key);
        int index = bucketIndex(h);
        for(Entry<K,V> e=this.bucket[index]; e!=null; e=e.next){
            if(h==e.hash && keyEquals(e.key, key)){
                e.value = value;
                return;
            }
        }
        Entry<K,V> newEntry = new Entry<>(key, value, h);
        newEntry.next = bucket[index]; // point newEntry to head
        bucket[index]=newEntry;
        this.size++;

        if(this.size> this.capacity * this.loadFactor){
            // resize when 75% of current capacity full means if current
            // capacity is 16 and total entries reached more than 16*0.75=12
            // double the size;
            int oldCapacity= this.capacity;
            this.capacity *=2;
            this.resize(oldCapacity);
        }
    }

    public V get(K key){
        int h = this.hash(key);
        int index = bucketIndex(h);
        for(Entry<K,V> e=this.bucket[index]; e!=null; e=e.next){
            if(h==e.hash && keyEquals(e.key, key)){
                return e.value !=null ? e.value : (V)"NULL";
            }
        }
        return null;
    }

    public boolean containsKey(K key){
        return this.get(key) != null;
    }

    public void remove(K key){
       int h = hash(key);
       int index = bucketIndex(h);
       Entry<K,V> prev = null;
       Entry<K,V> curr = this.bucket[index];
       while(curr!=null){
        if(curr.hash == h && keyEquals(curr.key, key)){
            if(prev==null) this.bucket[index]=curr.next;
            else prev.next=curr.next;
            this.size--;
            return;
        }
        prev=curr;
        curr=curr.next;
       }
       if(this.size>0 && this.size<this.capacity/4 && this.capacity> DEFAULT_CAPACITY){
            int oldCapacity= this.capacity;
            this.capacity /=2; // half the capaciy
            this.resize(oldCapacity);
       }
    }

    private void resize(int oldCapacity){
        Entry<K,V>[] newBucket = new Entry[this.capacity];
        for(int i=0;i<oldCapacity;i++){
            Entry<K,V> curr = this.bucket[i];
            while(curr!=null){
                int newIndex = this.bucketIndex(curr.hash);
                Entry<K,V> next = curr.next; // add head of new bucket at new index
                if(newBucket[newIndex]==null){
                    newBucket[newIndex]=curr;
                }
                else{
                curr.next=newBucket[newIndex];
                newBucket[newIndex]=curr;
                }
                
                curr=next;
            }
        }
        this.bucket=newBucket;
    }
}


