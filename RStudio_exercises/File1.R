print("Hello world")

#my first tibble
mytibble <- tibble(
  x = 1:4,
  y = x^2, 
  z = y + 0.1
)

mytibble

#my first tribble
mytribble <- tribble(
  ~x, ~y, ~z,
  1, 4.2,"a",
  3, 9.6,"b",
  4,16.8,"c"  
)
mytribble



#You might want to look at only part of a dataframe. 
#For example, I noticed the names of a number of models of cars in the mpg dataset, so let's 
#focus in on them. You can select a single column using the $ symbol

mpg$model


#If you want to select rows rather than columns from the dataframe, use square brackets []
  
mpg[194,]

#If you want a particular entry in the dataframe, provide a column number as well
  
mpg[194,2]

#And, of course, you can also look at a range of rows and columns as well
  
mpg[194:198,1:4]


#Tables and barcharts
#Looking at the mpg dataset, a good guide is that the columns containing characters <chr> 
#are categorical variables. Take a look at the first column, the manufacturer. 
#How many of each brand of car are there?
  
table(mpg$manufacturer)

#proportion of each type of car, among 234
prop.table(table(mpg$manufacturer))

#to represent this information as a bar chart
ggplot(mpg,aes(manufacturer)) + 
  geom_bar() + 
  theme(text = element_text(size = 30), axis.text.x = element_text(angle = 90))



library(tidyverse)

data <- starwars

#Table: number of characters by eye color
eye_counts <- data %>%
  count(eye_color, name = "n_characters")

eye_counts

#Table: proportions of characters by eye color

eye_props <- data %>%
  count(eye_color) %>%
  mutate(proportion = prop.table(n))

eye_props



library(tidyverse)

data <- storms


# Summarise wind speed: mean, standard deviation, and five-number summary
wind_summary <- data %>%
  summarise(
    mean_wind = mean(wind, na.rm = TRUE),          # Mean wind speed
    sd_wind = sd(wind, na.rm = TRUE),              # Standard deviation of wind speed
    min_wind = min(wind, na.rm = TRUE),            # Minimum wind speed
    q1_wind = quantile(wind, 0.25, na.rm = TRUE),  # 1st quartile (25%)
    median_wind = median(wind, na.rm = TRUE),      # Median (50%)
    q3_wind = quantile(wind, 0.75, na.rm = TRUE),  # 3rd quartile (75%)
    max_wind = max(wind, na.rm = TRUE)             # Maximum wind speed
  )

wind_summary

# Produce a histogram of wind speed
ggplot(data, aes(x = wind)) +
  geom_histogram(binwidth = 5, color = "black", fill = "lightblue") +
  labs(title = "Histogram of Storm Wind Speeds",
       x = "Wind Speed",
       y = "Count")

# Section 2: visualizing relationships


library(tidyverse)

# Load the storms dataset
data <- storms

# Scatterplot: wind speed vs pressure, coloured by storm status
ggplot(data, aes(x = wind, y = pressure, colour = status)) +
  geom_point() +
  labs(
    title = "Relationship Between Wind Speed and Storm Pressure",
    x = "Wind Speed",
    y = "Pressure",
    colour = "Storm Status"
  ) +
  theme_minimal()


library(tidyverse)

# Load the storms dataset
data <- storms

# Three scatterplots (one for each storm status) with trendlines
ggplot(data, aes(x = wind, y = pressure)) +
  geom_point(aes(colour = status)) +                  # Points coloured by status
  geom_smooth(method = "lm", se = FALSE) +           # Linear trendline
  facet_wrap(~ status) +                             # One panel for each status
  labs(
    title = "Wind Speed vs Pressure by Storm Status",
    x = "Wind Speed",
    y = "Pressure"
  ) +
  theme_minimal()




# Activity 4: Visualising common words and similarities between texts

# Part 1 — Load the CSV file
library(tidyverse)
library(tidytext)

# Check working directory
getwd()

# Load Carroll books from CSV (ensure carroll_books.csv is in the working directory)
carroll_books <- as_tibble(
  read.csv(
    file = "carroll_books.csv",
    as.is = c("text"),
    encoding = "UTF-8"
  )
)

carroll_books


#Part 2 — Count word frequencies (remove stop words)

# Tokenise the text column into individual words
words_df <- carroll_books %>%
  unnest_tokens(word, text)

# Remove English stop words
clean_words <- words_df %>%
  anti_join(stop_words, by = "word")

word_counts <- clean_words %>%
  count(word, sort = TRUE)

word_counts


#Visualisations for investigation
#Top 20 most common words

word_counts %>%
  slice_max(n, n = 20) %>%
  ggplot(aes(x = reorder(word, n), y = n)) +
  geom_col(fill = "steelblue") +
  coord_flip() +
  labs(
    title = "Top 20 Most Frequent Words",
    x = "Word",
    y = "Frequency"
  ) +
  theme_minimal()



#Section 3 manipulating and joining data

#Activity 5: Group by and summarise
library(tidyverse)
library(nycflights13)

# Part 1 ---------------------------------------------------------

carrier_delay <- flights %>%
  group_by(carrier) %>%
  summarise(
    mean_dep_delay = mean(dep_delay, na.rm = TRUE),
    n_flights = n()
  ) %>%
  arrange(desc(mean_dep_delay))

carrier_delay

#Plot: mean delay by carrier
ggplot(carrier_delay, aes(x = carrier, y = mean_dep_delay)) +
  geom_point(size = 3) +
  geom_smooth(se = FALSE, method = "loess") +
  labs(
    title = "Mean Departure Delay by Airline Carrier",
    x = "Carrier",
    y = "Mean Departure Delay (minutes)"
  )

#Part 2
carrier_flights_summary <- flights %>%
  group_by(carrier) %>%
  summarise(
    mean_dep_delay = mean(dep_delay, na.rm = TRUE),
    num_flights = n()
  ) %>%
  arrange(desc(num_flights))

carrier_flights_summary


#Plot: number of flights vs mean delay
ggplot(carrier_flights_summary, aes(x = num_flights, y = mean_dep_delay)) +
  geom_point(size = 3) +
  geom_text(aes(label = carrier), vjust = -0.8, size = 3) +
  geom_smooth(se = FALSE, method = "lm") +
  labs(
    title = "Relationship Between Flight Volume and Departure Delays",
    x = "Number of Flights",
    y = "Mean Departure Delay (minutes)"
  )


#Activity 6 — Join
library(tidyverse)
library(nycflights13)

# Part 1 ---------------------------------------------------------

# 1. Join flights with airlines (adds airline name)
flights_airlines <- flights %>%
  left_join(airlines, by = "carrier")

# 2. Join flights with airport info (origin airport)
flights_origin <- flights %>%
  left_join(airports, by = c("origin" = "faa"))

# 3. Join flights with airport info (destination airport)
flights_dest <- flights %>%
  left_join(airports, by = c("dest" = "faa"))

# 4. Join flights with weather data
flights_weather <- flights %>%
  left_join(weather, 
            by = c("origin", "year", "month", "day", "hour"))


# Part 2 ---------------------------------------------------------

# Select relevant columns in planes
planes_small <- planes %>%
  select(tailnum, manufacturer, model, year, type, engines, seats)

# Right join planes to flights using tailnum
flights_planes <- planes_small %>%
  right_join(flights, by = "tailnum")


# Activity 7 – Part 1: Exploring the gapminder dataset (2007)
# --------------------------------------------------------------

# 1. Load required packages ------------------------------------
# install.packages(c("gapminder", "dplyr", "ggplot2", "tidyr"))  # uncomment if needed
library(gapminder)   # contains the gapminder data.frame
library(dplyr)       # data manipulation
library(ggplot2)     # plotting
library(tidyr)       # reshaping data

# 2. Keep only the year 2007 -----------------------------------
gap_2007 <- gapminder %>%
  filter(year == 2007)

# 3. Compute the three possible quality-of-life indices ----------
gap_2007 <- gap_2007 %>%
  mutate(
    q1 = gdpPercap / lifeExp,                     # gdpPercap / lifeExp
    q2 = log(gdpPercap / lifeExp),                # log(gdpPercap / lifeExp)
    q3 = log(gdpPercap) / lifeExp                 # log(gdpPercap) / lifeExp
  )

# 4. Group by continent and calculate mean values ---------------
mean_by_cont <- gap_2007 %>%
  group_by(continent) %>%
  summarise(
    mean_pop   = sum(pop),                 # total population of the continent
    mean_q1    = mean(q1,   na.rm = TRUE),
    mean_q2    = mean(q2,   na.rm = TRUE),
    mean_q3    = mean(q3,   na.rm = TRUE),
    .groups = "drop"
  )

# 5. Barcharts: mean quality of life per continent -------------
# Reshape to long format for faceting
mean_long <- mean_by_cont %>%
  pivot_longer(
    cols = starts_with("mean_q"),
    names_to = "index",
    values_to = "mean_quality"
  ) %>%
  mutate(index = recode(index,
                        mean_q1 = "gdpPercap / lifeExp",
                        mean_q2 = "log(gdpPercap / lifeExp)",
                        mean_q3 = "log(gdpPercap) / lifeExp"))

# Plot
ggplot(mean_long, aes(x = continent, y = mean_quality, fill = continent)) +
  geom_col() +
  facet_wrap(~ index, scales = "free_y") +
  labs(
    title = "Mean Quality-of-Life Index by Continent (2007)",
    x = "Continent",
    y = "Mean Index Value"
  ) +
  theme_minimal() +
  theme(legend.position = "none",
        axis.text.x = element_text(angle = 45, hjust = 1))

# 6. Scatterplots: mean quality vs. continent population -------
# (one panel per index, with a linear trend line)
ggplot(mean_long, aes(x = mean_pop / 1e6, y = mean_quality, color = continent)) +
  geom_point(size = 3) +
  geom_smooth(method = "lm", se = FALSE, linetype = "dashed") +  # linear fit
  facet_wrap(~ index, scales = "free") +
  labs(
    title = "Mean Quality-of-Life vs. Continent Population (2007)",
    x = "Total Population (millions)",
    y = "Mean Index Value",
    color = "Continent"
  ) +
  theme_minimal()



#Section 4: Transforming data and dimension reduction

# Activity 8: Understanding Big Data with Small Data (PCA by hand)
# ===============================================================

# Given data
small_data <- data.frame(
  X = c(1,  1, -1,  1),
  Y = c(1, -1,  1,  1),
  Z = c(1,  1,  1, -1)
)

print(small_data)
#    X  Y  Z
# 1  1  1  1
# 2  1 -1  1
# 3 -1  1  1
# 4  1  1 -1

# ---------------------------------------------------------------
# Part 1: Scatterplots of Y vs X, Z vs X, Z vs Y
# ---------------------------------------------------------------
library(ggplot2)
library(gridExtra)   # for arranging multiple plots

p1 <- ggplot(small_data, aes(x = X, y = Y)) + 
  geom_point(size = 4, color = "blue") + 
  geom_text(label = 1:4, vjust = -1.5) +
  labs(title = "Y vs X") +
  theme_minimal()

p2 <- ggplot(small_data, aes(x = X, y = Z)) + 
  geom_point(size = 4, color = "red") + 
  geom_text(label = 1:4, vjust = -1.5) +
  labs(title = "Z vs X") +
  theme_minimal()

p3 <- ggplot(small_data, aes(x = Y, y = Z)) + 
  geom_point(size = 4, color = "darkgreen") + 
  geom_text(label = 1:4, vjust = -1.5) +
  labs(title = "Z vs Y") +
  theme_minimal()

gridExtra::grid.arrange(p1, p2, p3, ncol = 3)

# ---------------------------------------------------------------
# Part 2: Calculate the covariance matrix V of X, Y, Z
# ---------------------------------------------------------------
# Note: since n = 4 is very small, we use the sample covariance (divide by n-1)
V <- cov(small_data)       
round(V, 4)


# ---------------------------------------------------------------
# Part 3: Check by hand that columns of E are eigenvectors of V
# ---------------------------------------------------------------
E <- matrix(c(-1, -1,  1,
              0,  1,  1,
              1,  0,  1), 
            nrow = 3, byrow = FALSE)

colnames(E) <- paste0("PC", 1:3)
rownames(E) <- c("X", "Y", "Z")
print(E)

# Verify V %*% E == E %*% diag(eigenvalues)  (up to scaling & numerical tolerance)
eigen_decomp <- eigen(V)          # R's own calculation
eigenvectors_R <- eigen_decomp$vectors
eigenvalues_R  <- eigen_decomp$values

cat("\nEigenvalues from R:\n")
print(eigenvalues_R)


# The first two columns of E are eigenvectors for λ = 2
# Let's verify manually for the first eigenvector: [-1, 0, 1]^T

v1 <- E[,1]          # [-1, 0, 1]
V %*% v1          

cat("\nV * first column of E =\n")
print(V %*% v1)
cat("2 * first column of E =\n")
print(2 * v1)     

# Same for second column [-1, 1, 0]^T 
# Third column [1, 1, 1]^T → eigenvalue 0 (approximately)

# ---------------------------------------------------------------
# Part 4: Compute principal component scores P = D %*% E
# ---------------------------------------------------------------
D <- as.matrix(small_data)   # 4 × 3 data matrix

P <- D %*% E                 # This is the matrix of principal components

colnames(P) <- paste0("PC", 1:3)
rownames(P) <- paste0("Obs", 1:4)

cat("\n=== Principal Component Scores P = D %*% E ===\n")
print(round(P, 6))


# We have just performed PCA completely by hand!

# Optional: compare with R's built-in PCA
pca_R <- prcomp(small_data, scale. = FALSE)
cat("\nR's prcomp result (rotation matrix):\n")
print(pca_R$rotation)   # very similar to E (again, signs may differ)
cat("\nR's PC scores:\n")
print(pca_R$x)




# Section 5: Summarising data
# Activity 9 (confidence interval)

# Part 1 — Calculate the mean height (excluding NA values)

library(dplyr)

sample_mean <- mean(starwars$height, na.rm = TRUE)
sample_mean

# Part 2 — Calculate the standard deviation (excluding NAs)

sample_sd <- sd(starwars$height, na.rm = TRUE)
sample_sd

 
#Part 3 — Calculate the t-cutoff for a 90% CI

#A 90% CI leaves 5% in each tail?
#No — for a two-sided 90% CI, you leave 5% total, so 2.5% in each tail?
#Still no — because 90% CI → 10% outside → 5% in each tail.
# So we use:
  
p = 0.05

#Degrees of freedom = N − 1 (after removing NAs):
  
N <- sum(!is.na(starwars$height))

t_cutoff <- qt(p = 0.05, df = N - 1, lower.tail = FALSE)
t_cutoff

#Part 4 — Calculate the 90% confidence interval
# Use the standard formula: mean ± t * sd / sqrt(N)

lwr <- sample_mean - t_cutoff * sample_sd / sqrt(N)
upr <- sample_mean + t_cutoff * sample_sd / sqrt(N)

c(lwr = lwr, upr = upr)

#Part 5 — Upper bound only

upr
