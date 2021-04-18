# 此文件用于L8 Desugar Shrinking
# 包含整个Desugar Library, 以防止加载desugar的插件时已被加载的desugar class缺少方法
# 只在 Release Build 中使用

-keepclassmembers class * { *; }