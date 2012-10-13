#include <sstream>
#include <sys/time.h>

#include "cnn.h"
#include <log.h>

Cnn::Cnn()
{

} // Cnn

Cnn::~Cnn()
{
} // ~Cnn

bool Cnn::loadCascade(const char* filePath)
{
    return mFaceCascade.load(filePath);
} // Cnn::loadCascade

bool Cnn::loadConvNet(const char* filePath)
{
    using namespace std;
    ifstream ifs(filePath);
    const string xmlString((istreambuf_iterator<char> (ifs)),
                           istreambuf_iterator<char>());
    return mConvNet.fromString(xmlString);
} // Cnn::loadConvNet

std::vector<cv::Rect> Cnn::findFaces(cv::Mat const inputImage)
{
    std::vector<cv::Rect> faces;
    cv::Mat greyInputImage;
    cv::cvtColor(inputImage, greyInputImage, CV_BGR2GRAY);
    cv::equalizeHist(greyInputImage, greyInputImage);
    mFaceCascade.detectMultiScale(greyInputImage, faces, 1.1, 2, 2, cv::Size(30, 30));
    return faces;
} // Cnn::findFaces

void Cnn::drawRectangles(std::vector<cv::Rect> rectangles, cv::Mat frame)
{
    for (unsigned int rectIndex = 1; rectIndex < rectangles.size(); rectIndex++)
    {
        cv::Point topLeft(rectangles[rectIndex].x, rectangles[rectIndex].y);
        cv::Point bottomRight(rectangles[rectIndex].x + rectangles[rectIndex].width,
                              rectangles[rectIndex].y + rectangles[rectIndex].height);
        cv::rectangle(frame,
                      topLeft,
                      bottomRight,
                      cv::Scalar(0, 255, 0, 0), /* the color; green */
                      5, /* thickness */
                      8, /* line type */
                      0 /* shift */);
    } // for
} // Cnn::drawRectangles

cv::Mat Cnn::cropFrame(cv::Mat inputFrame, cv::Rect roi)
{
    cv::Mat scaled;
    cv::resize(inputFrame(roi), scaled, cv::Size(128, 128));
    return scaled;
} // Cnn::cropFrame

double Cnn::runConvNet(cv::Mat const frame)
{

    LOGI("Fface Native", "starting");
    timeval start_time;
    timeval end_time;
    double delta;
    gettimeofday(&start_time, NULL);
    LOGI("Fface Native", "timinig");
    cv::Mat greyInputImage;
    cv::cvtColor(frame, greyInputImage, CV_BGR2GRAY);
    LOGI("Fface Native", "color converted to grayscale");
    IplImage image = greyInputImage;
    CvMat *cvmat = cvCreateMat(image.height, image.width, CV_64FC1);
    cvConvert(&image, cvmat);
    cv::Mat floatMat = cvmat;
    floatMat -= 108.08409242f;
    floatMat /= 255.0f;
    LOGI("Fface Native", "forward propogating");
    double result = mConvNet.fprop(cvmat);
    gettimeofday(&end_time, NULL);
    delta = (end_time.tv_sec - start_time.tv_sec) * 1000.0;
    delta += (end_time.tv_usec - start_time.tv_usec) / 1000.0;
    std::ostringstream val;
    val << std::setprecision (5) << "Ran conv net in " << delta << " milliseconds";
    LOGI("Fface Native", val.str().c_str());
    return result;
} // Cnn::runConvNet

