############################################################
# Major Assignment 1 — CompThinking&BigData
############################################################

# Load required packages
library(nycflights13)   # dataset
library(tidyverse)      # for dplyr + ggplot2


############################################################
# -------------------- PART 1 ------------------------------
# Reading in the dataset and primary analysis
############################################################

# 1. Read in the flights dataset
data("flights")   # loads flights into the environment

# 2. Produce a table to summarise the origin variable
table(flights$origin)

# 3. Produce a bar chart of the origin variable
ggplot(flights, aes(x = origin)) +
  geom_bar() +
  labs(title = "Count of Flights by Origin Airport",
       x = "Origin Airport",
       y = "Number of Flights")

# 4. Calculate mean and standard deviation of distance
mean_distance <- mean(flights$distance, na.rm = TRUE)
sd_distance <- sd(flights$distance, na.rm = TRUE)

mean_distance
sd_distance

# 5. Produce a histogram of the distance variable
ggplot(flights, aes(x = distance)) +
  geom_histogram(binwidth = 100) +
  labs(title = "Histogram of Flight Distance",
       x = "Distance (miles)",
       y = "Frequency")


############################################################
# -------------------- PART 2 ------------------------------
# Visualising relationships
############################################################

# 1. Scatterplot of air_time against distance
ggplot(flights, aes(x = distance, y = air_time)) +
  geom_point(alpha = 0.3) +
  labs(title = "Scatterplot of Air Time vs Distance",
       x = "Distance (miles)",
       y = "Air Time (minutes)")

# 2. Contingency table of carrier and origin
table(flights$carrier, flights$origin)

# 3. Conditional table showing % of American Airlines (AA) flights departing JFK
prop.table(table(flights$carrier, flights$origin), 1)["AA", "JFK"] * 100

# 4. Side-by-side boxplots of distance for each origin
ggplot(flights, aes(x = origin, y = distance)) +
  geom_boxplot() +
  labs(title = "Distance Distribution by Origin Airport",
       x = "Origin Airport",
       y = "Distance (miles)")


############################################################
# -------------------- PART 3 ------------------------------
# Manipulating data
############################################################

# 1. Number of flights departing JFK in May 2013
flights %>%
  filter(origin == "JFK", month == 5) %>%
  summarise(count = n())

# 2. Identify carrier and airport with the first flight of 2013
flights %>%
  arrange(year, month, day, hour, minute) %>%
  slice(1) %>%
  select(carrier, origin)

# 3. Total metres travelled (1 mile = 1609.34 m)
flights %>%
  summarise(total_meters = sum(distance * 1609.34, na.rm = TRUE))

# 4. Total distance in miles for airlines with "Inc." in the name
airlines_inc <- airlines %>%
  filter(grepl("Inc\\.", name))

flights %>%
  filter(carrier %in% airlines_inc$carrier) %>%
  summarise(total_distance_miles = sum(distance, na.rm = TRUE))


############################################################
# -------------------- PART 4 ------------------------------
# Transforming data
############################################################

# 1. How many SD above the mean is the largest distance?
max_distance <- max(flights$distance, na.rm = TRUE)
sd_above_mean <- (max_distance - mean_distance) / sd_distance

sd_above_mean


############################################################
# -------------------- PART 5 ------------------------------
# Summarising data (95% CI)
############################################################

# Helper function to compute 95% CI under normality assumption
ci_95 <- function(x) {
  m <- mean(x, na.rm = TRUE)
  s <- sd(x, na.rm = TRUE)
  n <- sum(!is.na(x))
  error <- 1.96 * s / sqrt(n)
  
  c(lower = m - error,
    mean  = m,
    upper = m + error)
}

# 1. 95% CI for mean distance for all flights
ci_95(flights$distance)

# 2. Captioned table: mean + 95% CI for each carrier
carrier_ci <- flights %>%
  group_by(carrier) %>%
  summarise(
    mean_distance = mean(distance, na.rm = TRUE),
    lower_CI = ci_95(distance)[1],
    upper_CI = ci_95(distance)[3]
  )

carrier_ci