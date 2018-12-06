#include <jni.h>
#include <string>

extern "C"{
    extern int main(int argc,char * argv[]);//这个是bspatch.c中的方法
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_sven_bsdiff_MainActivity_bspatch(JNIEnv *env, jobject instance, jstring oldapk_,
                                                  jstring patch_, jstring output_) {
    const char *oldapk = env->GetStringUTFChars(oldapk_, 0);
    const char *patch = env->GetStringUTFChars(patch_, 0);
    const char *output = env->GetStringUTFChars(output_, 0);
    char * argv[4] = {"", const_cast<char *>(oldapk), const_cast<char *>(output),
                      const_cast<char *>(patch)};
    // TODO
    main(4,argv);//调用的就是bspatch.c中的main方法

    env->ReleaseStringUTFChars(oldapk_, oldapk);
    env->ReleaseStringUTFChars(patch_, patch);
    env->ReleaseStringUTFChars(output_, output);
}