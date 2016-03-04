export PATH=/opt/jdk1.8.0/bin:$PATH
export JAVA_HOME=/opt/jdk1.8.0
java -XshowSettings:properties -Xmx128M -DPI=true -DLWJGJ_BACKEND=GLES -Djava.library.path=lib:/opt/vc/lib:. -classpath .:lib:* org.pidome.client.photoframe.PhotoFrame $1