# SlidingTabLayout
Improved google's version of [SlidingTabLayout] (http://developer.android.com/samples/SlidingTabsBasic/src/com.example.android.common/view/SlidingTabLayout.html).

### Gradle
    
    repositories {
        maven{url "https://github.com/shaubert/maven-repo/raw/master/releases"}
    }
    dependencies {
        compile 'com.shaubert.ui.slidingtab:library:1.0.4'
    }

### Core changes:
 * This version listens for changes of `ViewPager`'s adapter.
 * You can setup colums stretching with `setStretchOption(StretchOption stretchOption)`. Where `StretchOption` could be one of following values:
   * `IF_LESS_THAN_3` the result equls to `ALWAYS` if count of columns <= 3, otherwise equls to `NONE`;
   * `IF_POSSIBLE` the result equls to `ALWAYS` if total width of columns < tab_layout_width, otherwise equls to `NONE`;
   * `ALWAYS` each columns will have width = tab_layout_width/columns_count;
   * `NONE` each colums will have width = wrap_content.
