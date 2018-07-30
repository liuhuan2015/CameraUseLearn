Android相机开发那些坑
>学习目标文章：[Android相机开发那些坑](https://zhuanlan.zhihu.com/p/20559606)，是一个知乎专栏。https://www.colabug.com/2319083.html
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
![自定义相机的开发流程](https://github.com/liuhuan2015/CameraUseLearn/blob/master/images/Custom_Camera_flow.jpg)<br>
* 创建预览类 创建继承自SurfaceView并实现SurfaceHolder接口的拍摄预览类。此类能够显示相机的实时预览图像。
* 建立预览布局 有了拍摄预览类，即可创建一个布局文件，将预览画面与设计好的用户界面控件融合在一起。
* 设置拍照监听器 给用户界面控件绑定监听器，使其能响应用户操作（如按下按钮）, 开始拍照过程。
* 拍照并保存文件 将拍摄获得的图像转换成位图文件，最终输出保存成各种常用格式的图片。
* 释放相机资源 相机是一个共享资源，必须对其生命周期进行细心的管理。当相机使用完毕后，应用程序必须正确地将其释放，以免其它程序访问使用时，发生冲突。

对应到代码编写上分为三个步骤：<br>

第一步 : 在AndroidManifest.xml中添加Camera相关功能使用的权限。<br>

第二步 : 编写相机操作功能类CameraOperationHelper。采用单例模式来统一管理相机资源，封装相机API的直接调用，并提供用于跟自定义相机Activity做UI交互的回调接口，<br>
其功能函数如下，主要有创建\释放相机，连接\开始\关闭预览界面，拍照，自动对焦，切换前后摄像头，切换闪光灯模式等，具体实现可以参考官方API文档。<br>

第三步 : 编写自定义相机Activity，主要是定制相机界面，实现UI交互逻辑，如按钮点击事件处理，icon资源切换，镜头尺寸切换动画等。<br>
这里需要声明一个SurfaceView对象来实时显示相机预览画面。通过SurfaceHolder及其Callback接口来一同管理屏幕surface和相机资源的连接，相机预览图像的显示/关闭。<br>

#### 四 . 开发过程中遇到的一些坑
**1. Activity设为竖屏时，SurfaceView预览图像颠倒90度。**

**屏幕方向** : 在Android系统中，屏幕的左上角是坐标系统的原点（0,0）坐标。原点向右延伸是X轴正方向，原点向下延伸是Y轴正方向。<br>

**相机传感器方向**：手机相机的图像数据都是来自于摄像头硬件的图像传感器，这个传感器在被固定到手机上后有一个默认的取景方向，<br>
如下图所示，坐标原点位于手机横放时的左上角，即与横屏应用的屏幕X方向一致。换句话说，与竖屏应用的屏幕X方向呈90度角。<br>
![相机传感器方向](https://github.com/liuhuan2015/CameraUseLearn/blob/master/images/Camera_Sensor_Orientation.jpg)<br>

在相机API中可以通过setDisplayOrientation()设置相机预览方向。在默认情况下，这个值为0，与图像传感器一致。<br>
因此对于横屏应用来说，由于屏幕方向和预览方向一致，预览图像不会颠倒90度。但是对于竖屏应用，屏幕方向和预览方向垂直，所以会出现颠倒90度现象。<br>
为了得到正确的预览画面，必须通过API将相机的预览方向旋转90，保持与屏幕方向一致。<br>

**相机的拍照方向**：当点击拍照按钮，拍摄的照片是由图像传感器采集到的数据直接存储到SDCard上的，因此，相机的拍照方向与传感器方向是一致的。<br>

**2. SurfaceView预览图像、拍摄照片拉伸变形**<br>

**SurfaceView尺寸**：即自定义相机应用中用于显示相机预览图像的View的尺寸，当它铺满全屏时就是屏幕的大小。这里surfaceview显示的预览图像暂且称作**手机预览图像**。<br>

**Previewsize**：相机硬件提供的预览帧数据尺寸。预览帧数据传递给SurfaceView，实现预览图像的显示。这里预览帧数据对应的预览图像暂且称作**相机预览图像**。<br>

**Picturesize**：相机硬件提供的拍摄帧数据尺寸。拍摄帧数据可以生成位图文件，最终保存成.jpg或者.png等格式的图片。这里拍摄帧数据对应的图像称作**相机拍摄图像**。<br>
图4说明了以上几种图像及照片之间的关系。手机预览图像是直接提供给用户看的图像，它由相机预览图像生成，拍摄照片的数据则来自于相机拍摄图像。<br>

预览图像变形，是因为SurfaceView和PreviewSize的长宽比率不一致。<br>

拍摄照片变形，是因为PreviewSize和PictureSize的长宽比率不一致。<br>

具体解决方法可以先通过camera.getSupportedPreviewSizes()和camera.getSupportedPictureSizes()获得相机硬件支持的所有预览和拍摄尺寸，<br>
然后在里面筛选出和SurfaceView的长宽比一致并且大小合适的尺寸，通过camera.setPrameters来更新设置。<br>

注意：市场上手机相机硬件支持的尺寸一般都是主流的4:3或者16:9，所以SurfaceView尺寸不能太奇葩，最好也设置成这样的长宽比。<br>

**3. 各种Crash**<br>
Camera的一些方法的执行是有先后顺序的，这个需要注意。<br>

在图像裁剪方面，如果是竖屏应用，裁剪区域的坐标系和相机传感器的坐标系是成90度角的，表现在裁剪里面就是：<br>
屏幕上的x方向，对应在拍摄图像上是高度方向，而屏幕上的y方向，对应到拍摄图像上则是宽度方向。因此在计算时要一定注意坐标系的转换以及越界保护。<br>

**4. 前置摄像头的镜像效果**<br>
Android相机硬件有个特殊设定，就是对于前置摄像头，在展示预览视图时采用类似镜面的效果，显示的是摄像头成像的镜像。<br>

**5. 锁屏下相机资源的释放问题**<br>
为了节省手机电量，不浪费相机资源，在开发的自定义相机里，如果预览图像已不需要显示，如按Home键盘切换后台或者锁屏后，此时就应该关闭预览并把相机资源释放掉。<br>

参考官方API文档，当surfaceView变成可见时，会创建surface并触发surfaceHolder.callback接口中surfaceCreated回调函数。<br>

而surfaceview变成不可见时，则会销毁surface，并触发surfacedestroyed回调函数。<br>

我们可以在对应的回调函数里，处理相机的相关操作，如连接surface、开启/关闭预览。 至于相机资源释放，则可以放在Acticity的onpause里执行。<br>

相应的，要重新恢复预览图像时，可以把相机资源申请和初始化放在Acticity的onResume里执行，然后通过创建surfaceview，将camera和surface相连并开启预览。<br>

但是在开发过程中发现，对于按HOME键切后台场景，程序可以正常运行。对于锁屏场景，则在重新申请相机资源时会发生crash，说相机资源访问失败.<br>



























                  


              


 
