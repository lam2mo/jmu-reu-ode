setwd('~/Documents/REU /Github Repo/General Solver')
DataFlame <- read.csv('FlameAllTests.dat', header = FALSE, sep = ' ', dec = '.')

#Adaptive 1
adaptive1 = DataFlame[(DataFlame[1] == "Adaptive1"),]


png("4_FlamePlotsAdaptive1.png", width = 1254, height = 614)

par(mfrow=c(1,2))
plot(adaptive1[,2],adaptive1[,3],main="Time vs Degree",ylab="Time(microseconds)",xlab="Degree")
plot(adaptive1[,2],adaptive1[,4],main="Number of Steps vs Degree",ylab="Time(microseconds)",xlab="Degree")

dev.off()

#Adaptive 2

png("4_FlamePlotsAdaptive2.png", width = 1254, height = 614)

adaptive2 = DataFlame[(DataFlame[1] == "Adaptive2"),]
par(mfrow=c(1,2))
plot(adaptive2[,2],adaptive2[,3], main="Time vs Degree",ylab="Time(microseconds)",xlab="Degree")
plot(adaptive2[,2],adaptive2[,4], main="Number of Steps vs Degree",ylab = "Time(microseconds)",xlab = "Degree")

dev.off()


