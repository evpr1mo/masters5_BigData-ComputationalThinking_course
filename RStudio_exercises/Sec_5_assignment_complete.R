###############################################
# Section 5 FINAL ASSIGNMENT SOLUTION
# Includes: population proportion, sampling,
# histograms, proportions for n=1000 & n=5000
###############################################

# -----------------------------
# 0. Load required packages
# -----------------------------
library(ape)
library(seqinr)
library(stringr)
library(ggplot2)

###############################################
# PART 1 — POPULATION PROPORTIONS (k = 5)
###############################################

# 1.1 Load chromosome 38 from dog genome
dog_ch38 <- ape::read.GenBank("NC_006620.3", as.character = TRUE)

# Convert from DNAbin -> character string, extract first 100,000 bp
dog_ch38 <- dog_ch38$NC_006620.3[1:100000]
dog_ch38 <- c2s(as.character(dog_ch38))
dog_ch38 <- str_to_upper(dog_ch38)

# Inspect first 200 nucleotides
str_sub(dog_ch38, 1, 200)


# -----------------------------
# PERFECT HASH FOR K-MER (k=5)
# -----------------------------
kmer_to_index <- function(kmer){
  n  <- str_length(kmer)
  letter_value <- c("A"=0, "C"=1, "G"=2, "T"=3)
  base <- 1
  index <- 1
  for(i in n:1){
    nucleotide <- str_sub(kmer, i, i)
    index <- index + base * letter_value[nucleotide]
    base <- base * 4
  }
  return(index)
}


# -----------------------------
# 1.2 Calculate population proportion of "AATAA"
# -----------------------------
k <- 5
target <- "AATAA"

# Initialise counter vector of length 4^5 = 1024
kmers <- numeric(4^k)

N <- str_length(dog_ch38)

# Count all 5-mers in the 100k population
for(i in 1:(N - k + 1)){
  kmer <- str_sub(dog_ch38, i, i + k - 1)
  index <- kmer_to_index(kmer)
  kmers[index] <- kmers[index] + 1
}

# population count of AATAA
target_index <- kmer_to_index(target)
population_count <- kmers[target_index]

# total number of 5-mers in population
population_total <- sum(kmers)

# Population proportion
population_proportion <- population_count / population_total
population_proportion



###############################################
# PART 2 — SAMPLE PROPORTIONS (n = 1000)
###############################################

# Sampling function provided
get_DNA_sample <- function(DNA, n){
  N <- str_length(DNA)
  start <- sample(1:(N - n + 1), size=1)
  return(str_sub(DNA, start, start + n - 1))
}

# 2.2 Set seed for reproducibility
set.seed(2017)

# Prepare storage for 100 sample proportions
sample_props_1000 <- numeric(100)

# 2.1 Loop through 100 random samples
for(j in 1:100){
  sample_seq <- get_DNA_sample(dog_ch38, n = 1000)
  
  # count AATAA in sample using brute force sliding window
  sample_count <- 0
  for(i in 1:(1000 - k + 1)){
    this_kmer <- str_sub(sample_seq, i, i + k - 1)
    if(this_kmer == target){
      sample_count <- sample_count + 1
    }
  }
  
  # sample proportion
  sample_props_1000[j] <- sample_count / (1000 - k + 1)
}

# 2.3 Histogram of n=1000 proportions
ggplot(data.frame(p = sample_props_1000), aes(x = p)) +
  geom_histogram(bins = 20, color="black", fill="lightblue") +
  ggtitle("Histogram of Sample Proportions (n = 1000)") +
  xlab("Sample proportion of AATAA") +
  ylab("Frequency")



###############################################
# PART 3 — SAMPLE PROPORTIONS (n = 5000)
###############################################

set.seed(2017)  # seed again for consistency

sample_props_5000 <- numeric(100)

for(j in 1:100){
  sample_seq <- get_DNA_sample(dog_ch38, n = 5000)
  
  sample_count <- 0
  for(i in 1:(5000 - k + 1)){
    this_kmer <- str_sub(sample_seq, i, i + k - 1)
    if(this_kmer == target){
      sample_count <- sample_count + 1
    }
  }
  
  sample_props_5000[j] <- sample_count / (5000 - k + 1)
}

# Histogram of n = 5000 proportions
ggplot(data.frame(p = sample_props_5000), aes(x = p)) +
  geom_histogram(bins = 20, color="black", fill="lightgreen") +
  ggtitle("Histogram of Sample Proportions (n = 5000)") +
  xlab("Sample proportion of AATAA") +
  ylab("Frequency")



###############################################
# PART 4 — COMPARISON
###############################################

mean_1000 <- mean(sample_props_1000)
sd_1000   <- sd(sample_props_1000)

mean_5000 <- mean(sample_props_5000)
sd_5000   <- sd(sample_props_5000)

mean_1000
sd_1000
mean_5000
sd_5000

# The comparison is a written answer:
# - Histogram for n=5000 is narrower (lower variance)
# - Mean for both should approach the population proportion
# - Larger samples → sample proportions closer to population