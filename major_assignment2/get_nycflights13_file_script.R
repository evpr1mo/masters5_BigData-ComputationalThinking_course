install.packages("nycflights13")
library(nycflights13)

#set filepath here
write.csv(
  flights,
  file = "G:/Computational Thinking and Big Data/Eclipse/eclipse_workspace/major_assignment2/src/major_assignment2/flights.csv",
  row.names = FALSE
)