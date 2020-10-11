学习笔记
HashMap的小结 + 源码分析
一、HashMap的原理

所谓Map，就是关联数组，存的是键值对——key&value。

实现一个简单的Map，你也许会直接用两个LIst，一个存key，一个存value。然后做查询或者get的时候，就遍历key的list，然后返回相应的value。

这样时间复杂度显然就是线性的，但这在map中已经是效率最低的get的方法了。而Hash主要提高效率的，也就是在这个位置——key的定位和查询这。

 

在数据结构中，我们学了hash这一技术，也就是散列表的技术。我们把整个表格看作是许多许多的空桶，然后散列函数也就是hash函数（拿质数来取模是一个很经典的hash的方法）会把你传入的参数处理后，散列到这些桶中。一个完美的哈希函数呢，是可以将你的输入无冲突的散列到表格中，也就是你传入的参数一人进一个桶，互相之间不冲突。但这是不可能的，然后数据结构中学到了很多处理冲突的方法，有链表处理法——就是在桶中冲突的元素用链表将他们存起来；还有线性探测法等，这些大概就是碰到冲突，然后根据一些算法来换位置，再冲突就再换。

 

Java的HashMap就是用散列表这一技术来存key和value。在HashMap中，我们hash函数的对象是Key instance，用一个Node[][]的二维数组来做table。这个Node是结点，一个Node代表着一个key和一个value，可以理解成HashMap中存储的一个对象。这个table可以看作是桶群，每个node都是一个桶。

当然传进hash函数的不可能直接是这个Key的实例对象，而是它的hashCode()方法产生的一个hash code。这个hashCode()方法是基类Object的一个方法，如果你不对这个方法进行重写的话，hashCode()方法返回的是根据这个对象的地址生成的一个int的散列码。

 

jdk对这个key instance的hashcode进行一个处理后，然后将它散列出一个值，作为桶的index，然后将这个key代表的node放进桶里面。对hashCode的处理会在下面的源码分析中介绍到。

 

jdk1.8后的hashMap还对数据结构做了一个处理，当一个桶冲突的链表太长了的时候，会把链表改成红黑树，然后当冲突小了的话，又会变回链表。

补充：看了源码后发现，只是对那个桶的链表进行转换。

 

tips：如果你的类直接当作key在hashMap中使用的话，equals和hashCode这两个方法用的都是Object默认的，也就是说主要比较的是对象的地址。如果你想根据你的类的实例的内容来进行散列的话，请重写hashCode；如果你想通过你的类的实例的内容来进行key的查找的话，请重写equals方法。

而且你重写的hashCode不一定要是unique的，但你重写的equals方法一定要严格区分不同的对象！！

可以参考String这个例子，这个类就可以很好地在hashMap中当key使用，它重写了hashCode()和equals()，都是根据String的内容来生成的。

 

 

二、源码重要部分解析

类的声明部分：



继承了AbstractMap，这是个Map接口的简单实现类。然后实现了Map、Cloneable接口（之前的博客有讲过，这里是为了实现浅复制）还有序列化的Serializable接口。

 

常量部分：

static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
 

首先这个capacity指的是HashMap中的桶表格——table （下面会说到）所分配的内存。所以这个常量的意思是table的初始length也就是初始化的长度，为16。

 

 static final int MAXIMUM_CAPACITY = 1 << 30;
 

table的capacity的最大值，如果用户在构造器中间接指明的的参数大于等于这个最大值的时候，table的capacity就会取这个。这个值是2的30次方。

这里我们会看到，而且源码中的备注中也有写到，这里的capacity一定要是2的整数次幂，因为这样才能很方便地用&、^等位操作符来进行一些运算，速度更快，下面的hash算法还有很多算法中会看到。

 

static final float DEFAULT_LOAD_FACTOR = 0.75f;
 

这个参数是加载因子，load_facotr = size/capacity，也就是map中存储的entry（键值对实体）除以table数组容量的值。这个值很重要，当这个facotr到达了这个数值，map就会进行扩容操作。如果在Map的构造器中没有特别指定load_factor，用的就是0.75.

 

static final int TREEIFY_THRESHOLD = 8;
 

这个值是说，table中，如果有桶（bucket）的链表的长度大于8，就有可能把所有的链表变成红黑树，是转变成树的一个阈值。

 

static final int UNTREEIFY_THRESHOLD = 6;
 

这个值是退化回链表的一个阈值，在扩容操作的时候，如果桶中的node数目小于6就变回链表喔。

 

static final int MIN_TREEIFY_CAPACITY = 64;
这个值是在转变成树之前，还会有一次判断，只有键值对数量大于 64 才会发生转换。这是为了避免在哈希表建立初期，多个键值对恰好被放入了同一个链表中而导致不必要的转化。

 

 

嵌套类Node的声明


 
/**
     * Basic hash bin node, used for most entries.  (See below for
     * TreeNode subclass, and in LinkedHashMap for its Entry subclass.)
     */
    static class Node<K,V> implements Map.Entry<K,V> {//这应该是普通节点的定义
        final int hash;
        final K key;
        V value;
        Node<K,V> next;

        Node(int hash, K key, V value, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey()        { return key; }
        public final V getValue()      { return value; }
        public final String toString() { return key + "=" + value; }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);//大概是想让node的hash值和key和value都有关系吧
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (Objects.equals(key, e.getKey()) &&
                    Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }
 
 

这个Node的声明在LinkedHashMap还有TreeNode中都会用到，是他们的爸爸和爷爷。

这个类就是一一个链表结点，是每个桶的最常规的结点类。

类变量有一个final的hash，代表的是这个Node的key instance的处理过的hash值。（就是这个Key instance的hashCode()值经过处理后的一个值，代表这个Key instance）；finall Key还有V value；Node next是指向下一个Node的指针，只有一个Next指针，可见这是个单向链表。

重写了hashCode()方法——key的hashCode()和value的hashCode()相亦或；重写了equals()方法——需要对key和value都进行比较。

 

 

hash方法——扰乱函数

 
static final int hash(Object key) {
        //扰乱函数  h无符号右移16后，相当于处以高位的16位被移到了低半段的16位，这个时候高半段都是0
        //然后再和本来的h做亦或，其实就是h的高半段和低半段做亦或（因为移位后的h前面都是0嘛），
        //这样就混合原始哈希码的高位和低位，以此来加大低位的随机性。而且混合后的低位掺杂了高位的部分特征，这样高位的信息也被变相保留下来。
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
 
 

在本文的HashMap的原理介绍中有提到，我们会对Key instance的hashCode()进行一个处理后再序列化到table的桶中，这个函数就可以看作是那个处理的算法。具体的分析都写在注解中了。

得到的是一个随机性很高的只有低半位的整形，后面序列的时候我们会用一个方法只取这个整形的低位部分。

 

 

一个很好玩的方法——tableSizeFor()

 
/**
     * Returns a power of two size for the given target capacity.
     */
    //给一个整数，返回大于输入参数且最接近的2整数次幂数
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        //累计移了31位，这段代码其实就是把最高位的1后面的全部变成1
        //0100变成0111，4变成7  全是1加个1就变成了2的整数幂了，7+1=8返回

        //所以第一步要先把参数减1，假如传进来是4，不减一的话就直接得到8返回显然错了
        //减一再处理就是3=0011，处理完就是返回0011+0001=0010=4正确
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
 
 

这个方法的作用和解析都写在注解中了。以前没怎么用过移位运算符的我觉得好厉害哈哈哈。

 

 

 

类变量声明部分

transient Node<K,V>[] table;//HashMap中的那个桶数组，键值对就是散列在这个Node，这个table分配的容量是capacity。
 

 

transient Set<Map.Entry<K,V>> entrySet;
 

这个类变量就是一个装了HashMap的所有键值对实体的一个Set。

 

 /**
 * The number of key-value mappings contained in this map.
 */
transient int size;
 

注释说的很清楚了，map中键值对的数量。

 

transient int modCount;
    //这个field是用来标识HashMap的结构被修改的的次数，比如键值对的添加或者是内部结构的改变，像覆盖update这些操作不算。
    //这个是用来给  iterators做fail-fast机制用的，就是iterator的时候如果HashMap还被别的进程改变会抛出异常的机制。
 

 

 
// (The javadoc description is true upon serialization.
    // Additionally, if the table array has not been allocated, this
    // field holds the initial array capacity, or zero signifying
    // DEFAULT_INITIAL_CAPACITY.)
    int threshold;
    //准备扩容的阈值吧    说如果table array没有分配，这个值是原始array的capacity喔
 
 

这个值是capacity*load_factor得到的值，也就是说，当HashMap中的键值对的数量超过这个的时候，就要考虑扩容了。

这个值其实困扰了我蛮久的，注解上说什么如果table还没allocated，就设置为默认的array capacity……这里还真有点不懂，是指initial capacity对应的threshold吗？

 

这个threshold其实应该理解成，这个HashMap应该容纳的node的个数，因为一超过这个值就要扩容嘛，相当于这个HashMap的“capacity”。而且我们注意，HashMap的类变量中没有capacity这个变量！！所以其实构造器还有初始化只是和这个threshold在打交道。

 

 

构造方法系列

HashMap中有四个构造方法：


 
 /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial
     * capacity and load factor.
     *
     * @param  initialCapacity the initial capacity
     * @param  loadFactor      the load factor
     * @throws IllegalArgumentException if the initial capacity is negative
     *         or the load factor is nonpositive
     */
    public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);
        this.loadFactor = loadFactor;
        this.threshold = tableSizeFor(initialCapacity);//一开始
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial
     * capacity and the default load factor (0.75).
     *
     * @param  initialCapacity the initial capacity.
     * @throws IllegalArgumentException if the initial capacity is negative.
     */
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }

    /**
     * Constructs a new <tt>HashMap</tt> with the same mappings as the
     * specified <tt>Map</tt>.  The <tt>HashMap</tt> is created with
     * default load factor (0.75) and an initial capacity sufficient to
     * hold the mappings in the specified <tt>Map</tt>.
     *
     * @param   m the map whose mappings are to be placed in this map
     * @throws  NullPointerException if the specified map is null
     */
    public HashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(m, false);
    }
 
 

第一个是最典型的，用户声明hashMap容量还有load_factor。注意，这个initialCapacity其实应该指的是HashMap的容量，因为它是经过tableSizeFor()处理后再赋值给threshold。

第二个只是指定了initialCapacity，直接调用了第一个构造器方法。

第三个是什么都没指定，构造器里只是对threshold进行了默认赋值，素以这个构造器调用的话，获得的HashMap的instance的threshold应该为0.

第四个传进来的是个Map，然后调用putMapEntries()方法。

补充：

（下划线部分有误）

后面发现这个initialCapacity还是设的是table的capacity，因为在resize()方法中，要给新的table分配内存的时候，用的就是这个initialCapacity，只不过用的是这个threshold来传值。所以第一点说的initialCapacity是不对的，它指的还是HashTable中的table的容量。

但threshold是这个hashMap的容量是没错的，因为确实超过这个值就要扩容或者进行操作。

 

 

putMapEntries()方法

如果是构造器调用的初始化的话，这个evict参数就为false，否则的话就是true（putAll方法就是true）

 
 final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
        int s = m.size();//传进来的map的键值对的个数
        if (s > 0) {
            if (table == null) { // pre-size
                //说明这个table还没分配内存初始化
                
                float ft = ((float)s / loadFactor) + 1.0F;
                //ft是，如果根据传进来的map的node的数量，创建table分配table，应该table的capacity是多少
                
                int t = ((ft < (float)MAXIMUM_CAPACITY) ?
                         (int)ft : MAXIMUM_CAPACITY);
                if (t > threshold)
                    
                    //给threshold赋值
                    threshold = tableSizeFor(t);
            }
            else if (s > threshold)
                
                //说明m的个数比所在map最多可以存储的node数量要多，所以要扩容或者是为table分配内存。
                resize();//这个方法可以扩容和为table分配初始的内存。
            
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                //利用entrySet获取这个Map中所有node的一个Set
                //然后调用putVal()方法来为本HashMap添加node，或者说添加键值对。
                
                K key = e.getKey();
                V value = e.getValue();
                putVal(hash(key), key, value, false, evict);
            }
        }
    }
 
 

这个方法是putAll()方法还有构造器传map进来的话，这两个方法的内部相关实现。

这个float ft = ((float)s / loadFactor) + 1.0F;这一行代码中为什么要加1.0F我不太懂，网上有说是可以节省一次resize()，也有说精确小数点后几位……emm不知道。

关于这个putVal()方法，在下面会介绍到。

 

 

 

get方法和getNode

 
 public V get(Object key) {
        //直接调用getNode()然后返回node的key
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

    /**
     * Implements Map.get and related methods
     *
     * @param hash hash for key 这个key instance的经过hash()过的hash值
     * @param key the key
     * @return the node, or null if none
     */
    final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {
            //进来后tab是当前table，n是table的capacity，first是这个key对应的那个位置的桶的第一个node
            
            if (first.hash == hash && // always check first node
                ((k = first.key) == key || (key != null && key.equals(k))))
                //看这个桶的第一个node的key是不是这个要get的key
                //因为有些对象的引用不一样，但equals是一样的，像是文字一样的String对象
                
                //看了后面的代码发现，很多涉及查找的都是这样先桶的第一个node进行比较。
                
                return first;
            if ((e = first.next) != null) {
                //第一个node不是要get的那个key，而且有下一个元素，也就是可能是链表也可能是树
                
                if (first instanceof TreeNode)//如果是树，则交给树的getTreeNode的实现来完成
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                do {
                    //这里就是这个桶下面有链表的情况，就遍历链表咯
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }
 
 

具体看里面的注解就行了。

 

 

put方法

 
 public V put(K key, V value) {
        //调用putVal方法
        return putVal(hash(key), key, value, false, true);
    }

    /**
     * Implements Map.put and related methods
     *
     * @param hash hash for key
     * @param key the key
     * @param value the value to put
     * @param onlyIfAbsent if true, don't change existing value这个值如果为true，就不改变原来的值，就如果put是覆盖原来的key的值，这个又为true的话就不能改变喔，虽然不知道哪里用hh
     * @param evict if false, the table is in creation mode.这个值如果为false的话，这个table就处于一个创造的模式，就是那个传map的构造器在初始化当前map，然后间接调用的这个方法。
     * @return previous value, or null if none
     */
    //put方法里面调用的后面的两个参数分别是false和true，说明可以覆盖原来的key的value；不是一个creation mode
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        if ((tab = table) == null || (n = tab.length) == 0)
            //进来这里的话说明这个table还没分配空间呢，tab是table，然后n是table的capacity
            n = (tab = resize()).length;//这一步相当于调用resize()方法为table分配内存并返回一个node数组作为table，然后将这个新的table的capacity给n
        
        if ((p = tab[i = (n - 1) & hash]) == null)
            //p是这个要put的key的对应的table中的那个桶的第一个node，i是这个key对应的那个桶的index
            //如果这个桶为空，说明还没冲突，就新建一个普通的结点。
            
            tab[i] = newNode(hash, key, value, null);
        else {
            //否则就是有冲突了，可能这个桶下是链表也可能是树
            
            Node<K,V> e; K k;
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                //先判断桶中的第一个node的key是不是这个要put的key，是的话将这个node赋值给e
                
                e = p;
            else if (p instanceof TreeNode)
                //如果第一个不是那个key，先看这个node是不是树，是的话交给树的操作。
                
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            
            else {
                //说明这个桶下面接的是链表，而且第一个不是正确的要put的key，那就遍历链表找咯
                
                for (int binCount = 0; ; ++binCount) {//遍历的时候要数着node的数量，因为可能添加的时候load_factor要超过，要变成树。
                    if ((e = p.next) == null) {//e往下遍历
                        //这句之后，e就是下一个结点，如果进入这个条件里面，说明链表已经到头了
                        //而且还没找到那个key，所以就插入新结点了
                        //插入后break，这时候e指向null
                        
                        p.next = newNode(hash, key, value, null);//这一步就是插入的那个语句
                        
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            //添加了新结点嘛，然后就要看这个桶的链表的node数量过多没，过多可能就要进行树化操作。
                            //在treeifyBin方法中还有个判断map中键值对总数超过64没的操作，超过了才树化。
                            
                            treeifyBin(tab, hash);//这个方法的操作是，把这个桶的单向链表变成树
                        break;
                    }
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        //如果遍历中发现有相同的key，就跳出来
                        //这个时候e指的是key和要put的那个key相等的node
                        break;
                    p = e;
                }
            }
            if (e != null) { // existing mapping for key
                //e不为null的话，说明上面的操作找到了 一个key和要put的可以一样的node
                //这个e指的就是那个key和put那个key一样的node
                
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)//条件允许，覆盖
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;//到了这里说明这个key还不存在，那么就要insert一个node，hashMap结构要改变所以这个值加一
        if (++size > threshold)
            //插入后size大于阈值，
            
            resize();
        afterNodeInsertion(evict);
        return null;
    }
 
 

 

关于这个treeifyBin()方法，下面也简单介绍下：

 
/**
     * Replaces all linked nodes in bin at index for given hash unless
     * table is too small, in which case resizes instead.
     */
    final void treeifyBin(Node<K,V>[] tab, int hash) {
        int n, index; Node<K,V> e;
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            resize();//这个情况下，还不至于进行结构的转换，只要扩容就好。
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            TreeNode<K,V> hd = null, tl = null;
            //树首结点还有树尾结点
            //其实就一个头节点，然后一个用来遍历的尾结点
            
            do {
                TreeNode<K,V> p = replacementTreeNode(e, null);//把这个结点转换为树结点
                if (tl == null)
                    hd = p;
                else {
                    p.prev = tl;
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null);
            //这一步做的，其实就是1.把所有的node结点变成TreeNode
            //2.把单向链表变成双向链表
            
            if ((tab[index] = hd) != null)
                //这一步就把桶的首元素变成这个双向链表的头结点
                
                hd.treeify(tab);//把这个双向链表变成树
        }
    }
 
 

一开始我以为，一旦发现需要变树，是把所有桶的链表都变成树，看到这里才发现只是变这个桶的链表。

可以看到，这里的treeifyBin()方法其实还不是把链表变成树，只是把结点都变成了TreeNode，然后还把单向链表变成了双向，然后最后调用

树头指针.treeify(tab)

这个语句才是把这个双向链表树化，应该是这样转换后，可以更方便地写转换成红黑树的代码吧。

关于红黑树的构造就不在这里细讲了。

 

 

 

然后是我们很重要的resize()方法


 
final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;//获取当前的桶表格table
        int oldCap = (oldTab == null) ? 0 : oldTab.length;//获取当前的table的capacity
        int oldThr = threshold;//获取当前的阈值，就是现在这个HashMap能够装几个node
        
        int newCap, newThr = 0;
        if (oldCap > 0) {
            //进入这里说明table已经有了，准备进行扩容
            
            //补充一下，如果当前有table，但table的capacity经过double后大于maxCap的话，下面两个if都不进入
            //这种情况经过下面两个if后，newCap的值为大于maxCap的double * oldCap；newThr为0
            //这个情况我们叫做%%，等等好描述
            
            if (oldCap >= MAXIMUM_CAPACITY) {
                //当前table的capacity大于maxCapability
                //就把阈值赋值为Integer的max
                //然后什么都不做返回当前table
                
                //一开始很奇怪，为什么当前capacity要和max比较，应该不可能大于max啊，后面分析其实是有可能的
                
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                //这个else应该就符合扩容的要求，准备扩容
                //进入这个if的话，就newCap是当前capacity的double而且小于maxCap
                //而且当前cap也正常，所以可以double
                
                //下面就阈值double，也就是newThr的值是当前的阈值的double
                
                newThr = oldThr << 1; // double threshold
        }
        else if (oldThr > 0) // initial capacity was placed in threshold
            //这里我们叫做情况#@好了，等等好描述
            //这个else是说明还没有table
            //这里的if说明用户在构造器中指定了initialCapacity
            
            newCap = oldThr;
            //在构造器代码中，用户将initialCapacity传给了threshold，
            //所以当前threshold传给这个新的capacity，完全没毛病
        
            //可见这个情况下，newThr的值还是0
        
        
        else {               // zero initial threshold signifies using defaults
            //这里就说明，没有table，而且构造器中没有指定threshold，也就是调用的是无参构造器
            //所以newCap和newThr都为默认值。
            
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
            //看上面的分析就知道，有两种情况newThr会为0：
            //#@的情况，也就是没有table，用户指定了initialCapacity
            //%%的情况，也就是有table但两倍oldCap大于MaxCap的情况
            
            
            //根据用户给的initialCapacity算threshold
            //或者根据大于maxCap的newCap算出它对应的threshold
            float ft = (float)newCap * loadFactor;
            
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
            //根据newCap给newThr赋值，newCap太大的话就给newThr赋值为整型的最大值
            
            //这里看到，如果是%%情况，newCap是不会变的，也就是还是>maxCap，所以是存在capacity大于maxCapacity的情况的，
            //但这个不怕它越界，因为即使是maxCap*2也不会大于Integer的maxValue
            
        }
        threshold = newThr;//好的，现在要更新这个阈值了
        
        @SuppressWarnings({"rawtypes","unchecked"})
            Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];//根据newCap来分配table的内存
        
        table = newTab;赋值给table，真正扩容或者分配内存
        
        if (oldTab != null) {
            //这里做的，是如果是扩容操作，那么要把旧的table中的东西迁移到现在的table中
            
            for (int j = 0; j < oldCap; ++j) {
                //对旧table的桶进行遍历
                
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                    //e是不为空的那个桶的首个node
                    
                    oldTab[j] = null;//将这个桶的引用置空，然后交给gc处理
                    
                    if (e.next == null)
                        //说明这个桶没有冲突，只有一个node
                        //那么就直接根据散列算法取hash低位然后散列到扩容后的table中去
                        newTab[e.hash & (newCap - 1)] = e;
                    
                    
                    else if (e instanceof TreeNode)
                        //这个else就意味着，桶下面的可能是链表或者树
                        //这里是树的情况，就交给树的split操作来处理
                        //这里应该有如果数量下来了然后又变回链表的操作
                        
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    
                    else { // preserve order
                        //这里是桶下面是个链表的情况，要做的是将这个桶下的链表变成两条链表，然后分别散列到新table中的两个桶中
                        
                        Node<K,V> loHead = null, loTail = null;//一号新链表的头指针和用来遍历的尾指针
                        Node<K,V> hiHead = null, hiTail = null;//二号新链表的头指针和用来遍历的尾指针
                        Node<K,V> next;//用来遍历原来旧表中那个桶的链表的指针
                        
                        do {
                            next = e.next;//next往下移
                            
                            
                            //这个e.hash & oldCap有两种情况，要么就等于oldCap，要么就等于0
                            //因为cap都是2的整数次幂嘛，所以都是10000的形式，也就只有最高位为1
                            //对旧链表中每个结点都进行这个&操作，就把node分成两类，形成两个链表
                            if ((e.hash & oldCap) == 0) {
                                //1号新链表的构造
                                
                                if (loTail == null)//第一次遍历，先把头指针赋值
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;//可见这里用的是尾插法，jdk7好像用的是头插法，然后头插法好像在多线程环境下可能变成环然后死循环。
                            }
                            else {
                                //2号新链表的构造
                                
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        
                        if (loTail != null) {
                            loTail.next = null;//2号新链表收尾
                            newTab[j] = loHead;//j是旧桶在旧table中的index，新table中的这个桶第一个node变成一号新链表的头指针
                        }
                        if (hiTail != null) {
                            hiTail.next = null;//2号新链表收尾
                            newTab[j + oldCap] = hiHead;//新table中的与1号链表相隔oldCap个距离的桶第一个node变成二号新链表的头指针
                        }
                    }
                }
            }
        }
        return newTab;
    }
 
 

最后讲一下这个新的两条链表的操作。

如果还是按照hash & cap - 1的操作的话，其实还是很不分散。因为你hash是不会变的嘛，然后cap在旧的基础上翻倍后，也就比之前多了一位。

也就是说hash & cap - 1就比之前那个旧的桶的index多了一位，那么原来桶中的这个index很可能仍然是旧的那个值。

 

而现在这个直接用旧桶的index然后另一个新链表的index为就桶index + oldCap的操作，就：

　　1. 让node在新table中的散列结果更分散。

　　2. 减少计算量。

 

 

putAll方法


 
/**
     * Copies all of the mappings from the specified map to this map.
     * These mappings will replace any mappings that this map had for
     * any of the keys currently in the specified map.
     *
     * @param m mappings to be stored in this map
     * @throws NullPointerException if the specified map is null
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        putMapEntries(m, true);
    }
 
 

调用putMapEntries()方法，然后这里不是初始化table，所以第二个参数传true。

 

 

remove方法


 
public V remove(Object key) {
        Node<K,V> e;
        return (e = removeNode(hash(key), key, null, false, true)) == null ?
            null : e.value;
    }

    /**
     * Implements Map.remove and related methods
     *
     * @param hash hash for key
     * @param key the key
     * @param value the value to match if matchValue, else ignored 如果下面那个参数为true，这个value参数就有用
     * @param matchValue if true only remove if value is equal 如果这个为true，那么只有在这个key对应的value和上面那个参数value相同才删除
     * @param movable if false do not move other nodes while removingfalse的话，删除的时候不移动其他node喔
     * @return the node, or null if none找不到node返回null
     */
    final Node<K,V> removeNode(int hash, Object key, Object value,
                               boolean matchValue, boolean movable) {
        Node<K,V>[] tab; Node<K,V> p; int n, index;
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (p = tab[index = (n - 1) & hash]) != null) {
            //tab为table，n为capacity，p为key那个桶位的第一个node
            
            
            Node<K,V> node = null, e; K k; V v;
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                //判断第一个是不是那个要删除的key的node，是的话赋值给node
                
                node = p;
            
            else if ((e = p.next) != null) {
                //如果第一个不是要删除的node
                //而且还有下一个，也就是下面可能是链表或者树
                
                if (p instanceof TreeNode)
                    //如果这是棵树，交给树的getTreeNode
                    
                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
                
                else {
                    //否则，这里就是个链表，那就遍历找要删除的那个key呗
                    do {
                        if (e.hash == hash &&
                            ((k = e.key) == key ||
                             (key != null && key.equals(k)))) {
                            //找到，就赋值给node
                            //然后break出来
                            
                            node = e;
                            break;
                        }
                        p = e;//p总是指向遍历中的那个e的爸爸，如果break出去的话，p就是要删除的那个node的爸爸
                    } while ((e = e.next) != null);
                }
            }
            
            if (node != null && (!matchValue || (v = node.value) == value ||
                                 (value != null && value.equals(v)))) {
                //node不为空，说明找到了要删除的node
                //而且如果matchValue，要删除的node的value和传进来那个相同
                
                if (node instanceof TreeNode)
                    //如果要删除的是树结点，交给树的操作
                    
                    //大概瞄了一眼，里面有如果node数量太少，变回链表的操作
                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
                
                else if (node == p)
                    //node不是树结点，而且要删除的node就是桶里面的首结点
                    //因为这个node==p只可能出现在，桶里面第一个元素就是要删除的node这种情况下
                    
                    tab[index] = node.next;//那么就桶的首元素的next直接指向node的下一个咯
                else
                    //不是树，而且要删除也不是第一个元素
                    //p是node的爸爸，素以直接p的next指向node的next
                    //剩下的就交给gc，Java就是爽哈哈哈
                    
                    p.next = node.next;
                
                ++modCount;//删除了结点，hashMap的结构肯定改变了啊，那就这个值加一
                --size;
                afterNodeRemoval(node);//一个linkedHashMap的回调函数
                return node;//返回删除的结点
            }
        }
        return null;
    }
 
 

 

Clear方法

就是遍历table然后把数组元素的引用都置空，gc舒服啊


 
 /**
     * Removes all of the mappings from this map.
     * The map will be empty after this call returns.
     */
    public void clear() {
        Node<K,V>[] tab;
        modCount++;
        if ((tab = table) != null && size > 0) {
            size = 0;
            for (int i = 0; i < tab.length; ++i)
                tab[i] = null;
        }
    }
 
 

containsKey和containsValue方法

containsKey源码：


 
 /**
     * Returns <tt>true</tt> if this map contains a mapping for the
     * specified key.
     *
     * @param   key   The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     */
    public boolean containsKey(Object key) {
        return getNode(hash(key), key) != null;
    }
 
containsKey方法就直接调用getNode方法，看找不找得到这个key的node

 

containsValue源码：


 
/**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value
     */
    public boolean containsValue(Object value) {
        Node<K,V>[] tab; V v;
        if ((tab = table) != null && size > 0) {
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    if ((v = e.value) == value ||
                        (value != null && value.equals(v)))
                        return true;
                }
            }
        }
        return false;
    }
 
这个可读性也很高，就是遍历table的每个桶，每个桶再单向遍历链表。当然，只有桶中有node才会去遍历链表。

但这里有个问题，如果桶里面是红黑树，这里为什么也能用单向链表e = e.next的方式去遍历？？

可能只有在迟点了解学习红黑树才能懂。


 

