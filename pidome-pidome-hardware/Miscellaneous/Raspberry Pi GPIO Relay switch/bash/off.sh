echo "Turning off"
echo "25" > /sys/class/gpio/export
echo "out" > /sys/class/gpio/gpio25/direction
echo "0" > /sys/class/gpio/gpio25/value