package com.wanpg.yauld.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Created by Shayabean on 2016/12/25.<br/>
 * 类引用关系分析器<br/>
 * 根据输入的类名集合与class文件目录集合，做输入类的引用分析
 */
public class ReferenceParser {

    private List<String> tarClasses;
    private List<String> classPath;
    private List<String> resultClass;

    private ClassPool pool;

    /**
     *
     * @param tarClasses 目标类类名，com.a.b
     * @param classPath class文件需要给出根目录，jar文件需要给出jar文件的绝对地址
     */
    public ReferenceParser(List<String> tarClasses, List<String> classPath) {
        this.tarClasses = tarClasses;
        this.classPath = classPath;
        resultClass = new ArrayList<>();
    }

    public List<String> parse() {
        pool = new ClassPool();
        for (String path : classPath) {
            try {
                pool.appendClassPath(path);
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
        matchReference(tarClasses);
        return resultClass;
    }

    private void matchReference(List<String> tarClasses) {
        for (String className : tarClasses) {
            CtClass cc = null;
            try {
                cc = pool.get(className);
            } catch (NotFoundException e) {
//                e.printStackTrace();
            }
            if (cc != null) {
                if (!resultClass.contains(className)) {
                    resultClass.add(className);
                    List<String> references = new ArrayList<String>();
                    references.addAll(cc.getRefClasses());
                    matchReference(references);
                }
            }
        }
    }

    public static List<String> formatPackageList2PathList(List<String> packageList, String endWith){
        ArrayList<String> result = new ArrayList<>();
        for (String packageName : packageList){
            result.add(packageName.replace(".", File.separator) + (endWith != null ? endWith : ""));
        }
        return result;
    }
}
