#include <new>

#include "uk_me_eldog_fface_ViewScoreActivity.h"
#include "cnn.h"

JNIEXPORT jdouble JNICALL Java_uk_me_eldog_fface_ViewScoreActivity_runConv
  (JNIEnv* env, jclass cls, jstring cnnFileName, jintArray bitmapData)
{
    const char *cnnFile;
    cnnFile = env->GetStringUTFChars(cnnFileName, 0);

    Cnn* cnn = new (std::nothrow) Cnn();
    if (cnn != 0)
    {
        if (!cnn->loadConvNet(cnnFile))
        {
            return (jdouble) -1000.0;
        } // if
    } // if
    else
    {
        return (jdouble) -1000.0;
    }

    jint* bgra = env->GetIntArrayElements(bitmapData, 0);

    cv::Mat mbgra(128, 128, CV_8UC4, (unsigned char *)bgra);
    double result = 0.1;
    result = cnn->runConvNet(mbgra);
    env->ReleaseIntArrayElements(bitmapData, bgra, 0);
    delete cnn;
    return (jdouble) result;
}

