package com.xiaoyinliuyun.launchmode2;

import java.io.Serializable;

/**
 * @Author yangkunjian.
 * @Date 2022/4/29 17:08.
 * @Desc
 */

public class Person implements Serializable {

    public String name;
    public int age;

    public Person() {
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
