Android平台Camera开发实践指南
>学习目标文章[Android平台Camera开发实践指南](https://juejin.im/post/5a33a5106fb9a04525782db5)
#### 一 . 前言
Android Camera相关API是Android生态碎片化最为严重的一块，首先Android本身就有两套API，<br>
Android 5.0以下的Camera和Android 5.0以上的Camera2，而且 更为严重的时，各家手机厂商对Camera2的支持程度也各不相同，这就导致我们在相机开发中要花费很大精力来处理兼容性问题。<br>

**相机开发的一般流程是什么？**<br>

 1. **检测并访问相机资源** 检查手机是否存在相机资源，如果存在则请求访问相机资源。<br>
 2. **创建预览界面** 创建继承自SurfaceView并实现SurfaceHolder接口的拍摄预览类。有了拍摄预览类，即可创建一个布局文件，将预览画面与设计好的用户界面控件融合在一起，实时显示相机的预览图像。
 3. **设置拍照监听器** 给用户界面控件绑定监听器，使其能响应用户操作, 开始拍照过程。
 4. **拍照并保存文件** 将拍摄获得的图像转换成位图文件，最终输出保存成各种常用格式的图片。
 5. **释放相机资源** 相机是一个共享资源，当相机使用完毕后，必须正确的将其释放，以免其它程序使用访问时发生冲突。
 
**相机开发一般需要注意哪些问题？**<br>

 1. **版本兼容问题** Android 5.0以下的Camera和Android 5.0以上的Camera2，Android 4.0以下的SurfaceView和Android 4.0以上的TextureView，Android 6.0 以上要做相机运行时权限校验。
 2. **设备兼容问题** Camera/Camera2里面的各种特性在有些手机厂商的设备实现方式和支持程度是不一样的，这个需要做兼容性测试，一点点踩坑。
 3. **各种场景下的生命周期变化问题** 最常见的是后台场景和锁屏场景，这两种场景下的相机资源的申请与释放，Surface的创建与销毁会带来一些问题。
 
**关于Camera/Camera2**<br>

 既然要解决这种兼容问题，就要两套并用，那是不是根据版本来选择呢：Android 5.0 以下用Camera，Android 5.0以上用Camera2呢？<br>
 
 事实上，这样是不可取的，前面说过不同手机厂商对Camera2的支持程度各不相同，即便是Android 5.0 以上的手机，也存在对Camera2支持非常差的情况，这个时候就要降级使用Camera，如何判断对Camera的支持 程度我们下面会说。<br>
 
**关于SurfaceView/TextureView** <br>

* SurfaceView是一个有自己Surface的View。界面渲染可以放在单独线程而不是主线程中。它更像是一个Window，自身不能做变形和动画。
* TextureView同样也有自己的Surface。但是它只能在拥有硬件加速层的Window中绘制，它更像是一个普通View，可以做变形和动画。 <br>

更多关于SurfaceView和TextureView区别的内容可以参考这篇文章[Android 5.0(Lollipop)中的SurfaceTexture，TextureView, SurfaceView和GLSurfaceView](https://blog.csdn.net/jinzhuojun/article/details/44062175)<br>
 
**那么如何针对版本进行方案的选择呢？**<br>
官方的开源库cameraview给出了方案：

 
 

 
 





