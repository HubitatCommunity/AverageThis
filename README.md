# AverageThis

Calculates a Rolling Average of a set of Illuminance Sensors into a virtual sensor of your choice.

Create a Virtual OmniSensor, or equivilant. 
In AverageThis, select your virtual device, then
Select one or more Illuminance sensors.
Click Done.

AverageThis receives every Illuminance update your selected devices send and adds it to the existing Average, per the formula:
"Thus the current cumulative average for a new datum point is equal to the previous cumulative average, times n, plus the latest datum point, all divided by the number of points received so far, n+1."

Your selected Illuminance devices establish the period or repetition of the samples added to the rolling average.

