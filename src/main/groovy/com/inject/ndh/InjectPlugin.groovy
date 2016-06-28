package com.inject.ndh

import com.pa.anydoor.Hack
import javassist.*
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class InjectPlugin implements Plugin<Project> {
//    Project mProject;
    InjectPluginExtension extension;
    String jarpath;
    String outputdir
    String androidhome
    String moduleName
    private String file
    String desJarName;
    Project mProject

    @Override
    void apply(Project project) {
        def dp = project.getDependencies();
        println("dependence=" + dp)
        mProject = project
        project.extensions.create("injectConfig", InjectPluginExtension)
        project.afterEvaluate {
            extension = project['injectConfig'];
            jarpath = extension.jarDir
            outputdir = extension.outputDir
            androidhome = extension.androidHome
            desJarName = jarpath.substring(jarpath.lastIndexOf("/") + 1);
            println(outputdir)
        }
        project.task("inject") << {
            createDir(project)
            initJar()
            reBuildJar(project)
        }
        project.tasks.getByName("build") {
            doLast {
                println("START INJECT1!!" + jarpath)
                createDir(project)
                initJar()
                reBuildJar(project)
            }
        }

    }

    def createDir(Project project) {
        project.exec {
            commandLine "mkdir", "-p", "${outputdir}"
        }
    }

    def reBuildJar(Project project) {
        println("${desJarName},${outputdir}")
        project.exec {
            commandLine "jar", "-cvfM", "${desJarName}", "-C", "${outputdir}", "."
        }
        project.exec {
            commandLine "mv", "${desJarName}", "${outputdir}"
        }
    }

    def initJar() {
        println("START INJECT!!" + jarpath)
        File jarFile = new File(jarpath);
        JarFile jar = new JarFile(jarFile);
        def optfile = new File(jarFile.getParent(), jarFile.name + ".opt1");
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optfile));
        Enumeration<?> enumeration = jar.entries();
        file = null
        ClassPool pool = ClassPool.getDefault();
        System.out.println("pool=" + pool);
        pool.insertClassPath(jarpath);
        pool.appendClassPath(new ClassClassPath(Hack.class));
        pool.appendClassPath(androidhome)

    delJarFile ( );
    System.out.println ( "添加classPath完成" )
    while ( enumeration.hasMoreElements ( ) ) {
        JarEntry jarEntry = (JarEntry) enumeration.nextElement();
        file = jarEntry.getName();
        ZipEntry zipEntry = new ZipEntry(file);
        InputStream inputStream = jar.getInputStream(jarEntry);
        jarOutputStream.putNextEntry(zipEntry)
        injectCode(jarOutputStream, (String) outputdir, (String) file, (ClassPool) pool, inputStream);
//            jarOutputStream.close();
//            inputStream.close();
    }
}

def injectCode(JarOutputStream jarOutputStream, String dir, String jarClassName,
               ClassPool pool, InputStream inputStream) {
    try {

//            jarOutputStream.write(Utils.toByteArray(inputStream))

        String srcName = jarClassName;
        if (jarClassName.endsWith(".class")) {
            jarClassName = jarClassName.replaceAll("/", ".");
            jarClassName = jarClassName.substring(0, jarClassName.lastIndexOf('.'));
            CtClass cClass = (pool).get(jarClassName);
            println(cClass.getClass().getName())
            if (cClass.isFrozen()) {
                cClass.defrost();
            }

            boolean flag = false;
            if (!flag && !cClass.isInterface() && !isAbstract(cClass) && !jarClassName.contains('$')) {
                System.out.println("cClass=" + cClass + " is the class we want");
                CtField cf = CtField.make("private static final boolean INJECT_FLAG = false;", cClass);
                cClass.addField(cf);
                CtMethod[] ms = cClass.getDeclaredMethods();
                if (null != ms && ms.length >= 1 && !isNativeMethod(ms[0])) {
                    System.out.println("ms=" + ms.length + ms[0].getName() + " ,isEmpty= " + ms[0].isEmpty());
                    ms[0].insertBefore("if(INJECT_FLAG) Class ss = com.pa.anydoor.Hack.class;");
                }
            } else {
                System.out.println("cClass=" + cClass.getSimpleName() + " not inject ");
            }
            if (cClass.isFrozen()) {
                cClass.defrost();
            }
            cClass.writeFile(dir);
            println("className=" + srcName);

        } else {

//                jarOutputStream.write(Utils.toByteArray(inputStream))
            if (!srcName.endsWith("/")) {
                println("不是class文件原样输出:srcName=" + srcName + ", dir=" + dir + "/" + srcName.substring(0, srcName.lastIndexOf("/") + 1) + ",filename=" + srcName.substring(srcName.lastIndexOf("/") + 1, srcName.length()))
                File temp_dir = new File(dir + "/" + srcName.substring(0, srcName.lastIndexOf("/")));
                boolean flag1 = temp_dir.mkdir();
                println("flag1=" + flag1)
                File file = new File(temp_dir, srcName.substring(srcName.lastIndexOf("/") + 1, srcName.length()));
                boolean flag = file.createNewFile();
                println("flag=" + flag);
                FileOutputStream out = new FileOutputStream(file);
                byte[] bs = Utils.toByteArray(inputStream);
                println("bs=" + bs.size())
                out.write(bs)
            }
        }


    } catch (Exception e) {

        println e.getMessage()
    }

}

def printlnx(String x) {
    printlnx(1)
}

def printlnx(int x) {
    println(x)
}


def isNativeMethod(CtMethod method) {
    System.out.println(method.getName() + ",isNative=" + Modifier.isNative(method.getModifiers()))
    return Modifier.isNative(method.getModifiers());
}

def isAbstract(CtClass ct) {
    System.out.println(ct.getName() + ",isNative=" + Modifier.isNative(ct.getModifiers()))

    return Modifier.isAbstract(ct.getModifiers());
}

def delJarFile = {
    File file = new File(jarpath);
    if (file != null && file.exists()) {
//           println("del file"+file.getName()+":"+file.delete());
    }
}
}