# SuperForwardView

## About
A lightweight android library that allows to you custom fast forward/rewind animation behaviours like netflix.

## GIF
<img src="https://github.com/ertugrulkaragoz/SuperForwardRewind/blob/master/GIF/SuperForwardRewind.gif"/>

## Design Credits
All design and inspiration credits belongs to [Netflix](https://play.google.com/store/apps/details?id=com.netflix.mediaclient&hl=tr).


## Setup
- Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
- Add the dependency
```gradle
dependencies {
        implementation 'com.github.ertugrulkaragoz:SuperForwardView:0.2'
}
```

## Usage

- Add `SuperForwardView` in your layout:
```xml
 Width / height must be equal to 2. Otherwise it will not work properly.
 <me.ertugrul.lib.Forward
        android:id="@+id/forward"
        android:layout_width="100dp"
        android:layout_height="50dp"
        app:f_color="#ffffff"
        app:f_textSize="15sp"
        app:f_arrowSize="10dp"
        app:f_strokeWidth="3dp" />

 <me.ertugrul.lib.Rewind
        android:id="@+id/rewind"
        android:layout_width="100dp"
        android:layout_height="50dp"
        app:r_color="#ffffff"
        app:r_textSize="15sp"
        app:r_arrowSize="10dp"
        app:r_strokeWidth="3dp" />
```
-   Customization:
```xml
    <me.ertugrul.lib.Forward
        android:id="@+id/forward"
        android:layout_width="100dp"
        android:layout_height="50dp"
        app:f_textSize="15sp"
        app:f_color="#ffffff"
        app:f_strokeWidth="3dp"
        app:f_textInput="10"
        app:f_animationDuration="1000"
        app:f_scalePercent="16"
        app:f_arcRotationAngle="60"
        app:f_sweepAngle="300"
        app:f_arrowSize="10dp" />

   <me.ertugrul.lib.Rewind
        android:id="@+id/forward"
        android:layout_width="100dp"
        android:layout_height="50dp"
        app:r_textSize="15sp"
        app:r_color="#ffffff"
        app:r_strokeWidth="3dp"
        app:r_textInput="10"
        app:r_animationDuration="1000"
        app:r_scalePercent="16"
        app:r_arcRotationAngle="60"
        app:r_sweepAngle="300"
        app:r_arrowSize="10dp" />
```

-   Get notified when `SuperForwardView` animation starts or ends by callback:
```kotlin
	forward.onAnimationStart = {
            Log.e("onAnimationStart", "started")
        }
        forward.onAnimationEnd = {
            Log.e("onAnimationEnd", "ended")
        }
	
	rewind.onAnimationStart = {
            Log.e("onAnimationStart", "started")
        }
        rewind.onAnimationEnd = {
            Log.e("onAnimationEnd", "ended")
        }
```
Or set a listener
```kotlin 
	forward.setOnAnimationStartListener(object : OnAnimationStartListener {
            override fun onAnimationStart() {
                Log.e("OnAnimationStartListenr", "started")
            }
        })
        forward.setOnAnimationEndListener(object : OnAnimationEndListener {
            override fun onAnimationEnd() {
                Log.e("OnAnimationEndListenr", "ended")
            }
        })
	
	rewind.setOnAnimationStartListener(object : OnAnimationStartListener {
            override fun onAnimationStart() {
                Log.e("OnAnimationStartListenr", "started")
            }
        })
        rewind.setOnAnimationEndListener(object : OnAnimationEndListener {
            override fun onAnimationEnd() {
                Log.e("OnAnimationEndListenr", "ended")
            }
        })
```



- E-mail: ertugrulkaragoz12@gmail.com
- Twitter: [@ertugruIkaragoz](https://twitter.com/ertugruIkaragoz)

## License
```
MIT License

Copyright (c) 2021 Ertuğrul Karagöz

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
