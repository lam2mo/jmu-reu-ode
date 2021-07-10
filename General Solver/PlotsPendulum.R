setwd('~/Documents/REU /Github Repo/General Solver')
DataFlame <- read.csv('PendulumAllTests.dat', header = FALSE, sep = ' ', dec = '.')

#Adaptive 1
adaptive1 = DataFlame[(DataFlame[1] == "Adaptive1"),]
par(mfrow=c(1,2))
plot(adaptive1[,2],adaptive1[,3],main="Time vs Degree",ylab="Time(microseconds)",xlab="Degree")
plot(adaptive1[,2],adaptive1[,4],main="Number of Steps vs Degree",ylab="Time(microseconds)",xlab="Degree")


#Adaptive 2
adaptive2 = DataFlame[(DataFlame[1] == "Adaptive2"),]
par(mfrow=c(1,2))
plot(adaptive2[,2],adaptive2[,3], main="Time vs Degree",ylab="Time(microseconds)",xlab="Degree")
plot(adaptive2[,2],adaptive2[,4], main="Number of Steps vs Degree",ylab = "Time(microseconds)",xlab = "Degree")


#Adaptive 3
adaptive3 = DataFlame[(DataFlame[1] == "Adaptive3"),]
par(mfrow=c(1,2))
plot(adaptive3[,2],adaptive3[,3], main="Time vs Degree",ylab="Time(microseconds)",xlab="Degree")
plot(adaptive3[,2],adaptive3[,4], main="Number of Steps vs Degree",ylab = "Time(microseconds)",xlab = "Degree")


