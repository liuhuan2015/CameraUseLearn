Android相机开发那些坑
>学习目标文章：[Android相机开发那些坑](https://zhuanlan.zhihu.com/p/20559606)，是一个知乎专栏
#### 一 . Android中开发相机应用的两种方式
Android系统提供了两种使用手机相机资源实现拍摄功能的方法，<br>

一种是直接通过Intent调用系统相机组件，这种方法快速方便，适用于直接获得照片的场景，如上传相册，微博、朋友圈发照片等;<br>

另一种是使用相机API来定制自定义相机，这种方法适用于需要定制相机界面或者开发特殊相机功能的场景，如需要对照片做裁剪、滤镜处理，添加贴纸，表情，地点标签等;<br>

本篇文章主要是从如何使用相机API来定制自定义相机这个方向展开的。

#### 二 . 相机API中关键类解析
通过相机API实现拍摄功能涉及以下几个关键类和接口：<br>

**Camera**：最主要的类，用于管理和操作camera资源。它提供了完整的相机底层接口，支持相机资源切换，设置预览/拍摄尺寸，<br>
设定光圈、曝光、聚焦等相关参数，获取预览/拍摄帧数据等功能，主要方法有以下这些：

* open() : 获取camera实例
* setPreviewDisplay(SurfaceHolder) : 绑定绘制预览图像的surface。surface是指向屏幕窗口原始图像缓冲区（raw buffer）的一个句柄，<br>
通过它可以获得这块屏幕上对应的canvas，进而完成在屏幕上绘制View的工作。通过surfaceHolder可以将Camera和surface连接起来，<br>
当camera和surface连接后，camera获得的预览帧数据就可以通过surface显示在屏幕上了。
* setParameters(Parameters params) : 设置相机参数，包括前后摄像头，闪光灯模式、聚焦模式、预览和拍照尺寸等。
* startPreview() : 开始预览，将camera底层硬件传来的预览帧数据显示在绑定的surface上。
* stopPreview() : 停止预览，关闭camra底层的帧数据传递以及surface上的绘制。
* release() : 释放Camera实例
* takePicture(ShutterCallback shutter, PictureCallback raw,
              PictureCallback jpeg) : 这个是实现相机拍照的主要方法，包含了三个回调参数。shutter是快门按下时的回调，<br>
              raw是获取拍照原始数据的回调，jpeg是获取经过压缩成jpg格式的图像数据的回调。

**SurfaceView**:用于绘制相机预览图像的类，提供给用户实时的预览图像。<br>
普通的View以及派生类都是共享同一个surface的，所有的绘制都必须在UI线程中进行。而SurfaceView是一种比较特殊的View,它并不与其它普通View共享surface，<br>
而是在内部持有一个独立的surface，SurfaceView负责管理这个surface的格式、尺寸、以及显示位置。<br>

由于UI线程还要同时处理其他交互逻辑，因此对view的更新速度和帧率无法保证，而Surfaceview由于持有一个独立的surface，<br>
因而可以在独立的线程中进行绘制，因此可以提供更高的帧率。<br>

自定义相机的预览图像由于对更新速度和帧率要求比较高，所以比较适合用surfaceview来显示。

**SurfaceHolder**:SurfaceHolder是控制surface的一个抽象接口，它能够控制surface的尺寸和格式，修改surface的像素，监视surface的变化等等，<br>
SurfaceHolder的典型应用就是用于SurfaceView中。SurfaceView通过getHolder()方法获得SurfaceHolder 实例，通过后者管理监听surface 的状态。

**SurfaceHolder.Callback接口**:负责监听surface状态变化的接口，有三个方法：<br>
* surfaceCreated(SurfaceHolder holder) : 在surface创建后立即被调用。在开发自定义相机时，可以通过重载这个函数调用camera.open()、camera.setPreviewDisplay()，<br>
来实现获取相机资源、连接camera和surface等操作
* surfaceChanged(SurfaceHolder holder, int format, int width,
                  int height) : 在surface发生format或size变化时调用。在开发自定义相机时，可以通过重载这个函数调用camera.startPreview来开启相机预览，<br>
                  使得camera预览帧数据可以传递给surface，从而实时显示相机预览图像。
* surfaceDestroyed(SurfaceHolder holder) : 在surface销毁之前被调用。在开发自定义相机时，可以通过重载这个函数调用camera.stopPreview()，camera.release()来实现停止相机预览及释放相机资源等操作。
#### 三 . 自定义相机的开发过程
定制一个自定义相机应用，通常需要完成以下步骤，其流程图如图所示：<br>
![自定义相机的开发流程](https://github.com/liuhuan2015/CameraUseLearn/blob/master/images/Custom_Camera_flow.jpg)

                  


              


 
