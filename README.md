# AverageThis Application

Calculates a Rolling Average of Illuminance, Temperature and Relative Humidity from a set of Omni Sensors into a virtual sensor of your choice.
<p>
Create a Virtual OmniSensor, or equivilant. 
In AverageThis, create a child instance 
Select your virtual device, then
Select one or more Illuminance sensors.
Rename the child as needed.
Click Done.
<p>
AverageThis receives every Illuminance, Temperature and Relative Humidity update from your selected devices and adds it to the existing Average, per the formula:
"avg -= avg / N;
 avg += new_sample / N;"
Where N is 200, approximating 200 samples into the past.
<p>
Your selected Illuminance devices establish the period or repetition of the samples added to the rolling average.

