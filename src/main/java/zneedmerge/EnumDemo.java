package zneedmerge;

public enum EnumDemo {

    Constants_A("枚举成员A"),
    Constants_B("枚举成员B"),
    Constants_C("枚举成员C"),
    Constants_D(3);

    private String description;
    private int i = 4;

    /**
     * 在枚举类型中，可以添加构造方法，但是规定构造方法必须为 private 修饰符所修饰。并且在使用枚举类时，构造方法会被自动调用，
     * 并且也是只调用一次，也可以应用这个特性实现单例设计模式。
     *
     * 枚举被设计成是单例模式，即枚举类型会由JVM在加载的时候，实例化枚举对象，你在枚举类中定义了多少个就会实例化多少个，JVM为
     * 了保证每一个枚举类元素的唯一实例，是不会允许外部进行new的，所以会把构造函数设计成private，防止用户生成实例，破坏唯一性。
     *
     * 就是为了防止客户代码实例化一个枚举对象。
     * 枚举类型是单例模式的。你需要实例化一次，然后再整个程序之中就可以调用他的方法和成员变量了。枚举类型使用单例模式是因为他
     * 的值是固定的，不需要发生改变。
     */
    private EnumDemo() {
    }

    private EnumDemo(String description) {
        this.description = description;
    }

    private EnumDemo(int i) {
        this.i = i;
    }

    public String getDescription() {
        return description;
    }

    public int geti() {
        return i;
    }


    public static void main(String[] args) {
        // 遍历枚举类中所有的枚举项，
        for (int i = 0; i < EnumDemo.values().length; i++) {
            System.out.println(EnumDemo.values()[i] + " 值为：" + EnumDemo.values()[i].getDescription());
        }

        for (EnumDemo demo : EnumDemo.values()) {
            System.out.println(demo + " 值为：" + demo.getDescription());
        }
        System.out.println(EnumDemo.valueOf("Constants_D") + " 值为：" + EnumDemo.valueOf("Constants_D").geti());
    }
}
