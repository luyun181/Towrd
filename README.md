# Towrd
this is a POI test

解决方案是：在 app/build.gradle 中添加如下配置,放在android｛  里面跟defaultconfig  buildtype等配置是同级

    lintOptions {
        abortOnError false
    }
    dexOptions {
        preDexLibraries = false
        javaMaxHeapSize "4g"
    }
    project.tasks.withType(com.android.build.gradle.tasks.Dex) {
        additionalParameters = ['--core-library']
    }
    packagingOptions {
        exclude 'META-INF/LICENSE'
        exclude 'META-INF/NOTICE'
        exclude 'META-INF/LICENSE.txt'
        exclude 'META-INF/NOTICE.txt'
    }

目前存在的问题是这样的：

    ProGuard, version 5.2.1
    Reading program jar [F:\Towrd\app\build\intermediates\transforms\jarMerging\debug\jars\1\1f\combined.jar]
    Reading library jar [E:\adt-bundle-windows-x86_64-20140702\sdk\build-tools\21.1.1\lib\shrinkedAndroid.jar]
    Preparing output jar [F:\Towrd\app\build\intermediates\multi-dex\debug\componentClasses.jar]
      Copying resources from program jar [F:\Towrd\app\build\intermediates\transforms\jarMerging\debug\jars\1\1f\combined.jar]
    :app:transformClassesWithDexForDebug
    Running dex in-process requires build tools 23.0.2.
    For faster builds update this project to use the latest build tools.
    Error:warning: Ignoring InnerClasses attribute for an anonymous inner class
    Error:(org.apache.xmlbeans.XmlBeans$1) that doesn't come with an
    Error:associated EnclosingMethod attribute. This class was probably produced by a
    Error:compiler that did not target the modern .class file format. The recommended
    Error:solution is to recompile the class from source, using an up-to-date compiler
    Error:and without specifying any "-target" type options. The consequence of ignoring
    Error:this warning is that reflective operations on this class will incorrectly
    Error:indicate that it is *not* an inner class.

    Error:1 error; aborting
    :app:transformClassesWithDexForDebug FAILED
    Error:Execution failed for task ':app:transformClassesWithDexForDebug'.
    > com.android.build.api.transform.TransformException: com.android.ide.common.process.ProcessException: java.util.concurrent.ExecutionException: com.android.ide.common.process.ProcessException: org.gradle.process.internal.ExecException: Process 'command 'F:\Program Files\Java\jdk1.7.0_80\bin\java.exe'' finished with non-zero exit value 1
    Information:BUILD FAILED
    Information:Total time: 26.646 secs
    Information:178 errors
    Information:0 warnings
    Information:See complete output in console



