[Android 开发实践：屏幕旋转的处理](http://www.linuxidc.com/Linux/2013-09/90534.htm)

1. 不做任何处理的情况下

    默认情况下，当用户手机的重力感应器打开后，旋转屏幕方向，会导致 app 的当前 activity 发生 onDestroy-> onCreate，会重新构造当前 activity 和界面布局，很多横屏 / 竖屏的布局如果没有很好的设计的话，转换为竖屏 / 横屏后，会显示地很难看。

    如果想很好地支持屏幕旋转，则建议在 res 中建立 layout-land 和 layout-port 两个文件夹，把横屏和竖屏的布局文件放入对应的 layout 文件夹中。

2. 如何设置固定的屏幕方向

    在 AndroidManifest.xml 对应的 activity 属性中，添加：

        android:screenOrientation="landscape" // 横屏
        android:screenOrientation="portrait"  // 竖屏

    默认的情况下，应用启动后，会固定为指定的屏幕方向，即使屏幕旋转，Activity 也不会出现销毁或者转向等任何反应。

3. 强制开启屏幕旋转效果

    如果手机没有开启重力感应器或者在 AndroidManifest.xml 中设置了 android:screenOrientation，该 Activity 不会响应屏幕旋转事件。

    如果在上述情况下，依然希望 Activity 能响应屏幕旋转，则添加如下代码：

        // activity 的 onCreate 函数中
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

4. 屏幕旋转时，不希望 activity 被销毁

    如果希望捕获屏幕旋转事件，并且不希望 activity 被销毁，方法如下：

    * （1）在 AndroidManifest.xml 对应的 activity 属性中，添加：

            android:configChanges="orientation|screenSize"

    * （2）在对应的 activity 中，重载函数 onConfigurationChanged

            @Override
            public void onConfigurationChanged(Configuration newConfig) {
                super.onConfigurationChanged(newConfig);
            }

    在该函数中可以通过两种方法检测当前的屏幕状态：

    * 第一种：

        判断 newConfig.orientation 是否等于 Configuration.ORIENTATION_LANDSCAPE，Configuration.ORIENTATION_PORTRAIT

        当然，这种方法只能判断屏幕是否为横屏，或者竖屏，不能获取具体的旋转角度。

    * 第二种：

        调用 this.getWindowManager().getDefaultDisplay().getRotation();

        该函数的返回值，有如下四种：

        Surface.ROTATION_0，Surface.ROTATION_90，Surface.ROTATION_180，Surface.ROTATION_270

        其中，Surface.ROTATION_0 表示的是手机竖屏方向向上，后面几个以此为基准依次以顺时针 90 度递增。

        *这种方法的 Bug*

        最近发现这种方法有一个 Bug，它只能一次旋转 90 度，如果你突然一下子旋转 180 度，onConfigurationChanged 函数不会被调用。

5. 获取屏幕旋转方向

		this.getResources().getConfiguration().orientation
		
6. 横屏时隐藏导航栏，进入全屏状态

		Boolean isLandscape = (config.orientation == Configuration.ORIENTATION_LANDSCAPE);

        bannerView.setVisibility(isLandscape ? View.GONE : View.VISIBLE);
        if (isLandscape) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }