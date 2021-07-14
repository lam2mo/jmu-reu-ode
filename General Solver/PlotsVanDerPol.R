setwd('~/Documents/REU /Github Repo/General Solver')
DataFlame <- read.csv('VanDerPolAllTests.dat', header = FALSE, sep = ' ', dec = '.')


adaptive1 = DataFlame[(DataFlame[1] == "Adaptive1"),]
adaptive2 = DataFlame[(DataFlame[1] == "Adaptive2"),]


png("4_VanderpolPlots.png", width = 1254, height = 614)

x = 1.2;
minY = min(adaptive1[,3], adaptive2[,3])
maxY = max(adaptive1[,3], adaptive2[,3])

par(mfrow=c(1,2))
plot(adaptive1[,2],adaptive1[,3],main="Time vs Degree",ylab="Time(microseconds)",xlab="Degree", cex.lab = x,
     col=rgb(0.2,0.4,0.1,0.7), ylim = c(minY,maxY))
points(adaptive2[,2],adaptive2[,3], pch = 2,col=rgb(0.8,0.4,0.1,0.7))
legend("topleft",
       c("KR","Warne"),cex = x, pch = c(1,2), 
       col = c(rgb(0.2,0.4,0.1,0.7),col=rgb(0.8,0.4,0.1,0.7))
)


plot(adaptive1[,2],adaptive1[,4],main="Number of Steps vs Degree",ylab="Number of Steps",xlab="Degree", cex.lab = x,col=rgb(0.2,0.4,0.1,0.7))
points(adaptive2[,2],adaptive2[,4], pch = 2,col=rgb(0.8,0.4,0.1,0.7))
legend("topright",
       c("KR","Warne"), pch = c(1,2), cex = x,  col = c(rgb(0.2,0.4,0.1,0.7),col=rgb(0.8,0.4,0.1,0.7))
)
dev.off()




