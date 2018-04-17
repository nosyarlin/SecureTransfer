library(tidyverse)
library(plotly)

cp1 <- read.csv('/Users/arroyo/Desktop/stuff/Term 5/50005/programming_assignment2/SecureTransfer/cp1-data.csv')
cp2 <- read.csv('/Users/arroyo/Desktop/stuff/Term 5/50005/programming_assignment2/SecureTransfer/cp2-data.csv')

cp1.linearModel = lm(time ~ Size, data=cp1)
cp2.linearModel = lm(time ~ Size, data=cp2)

x <- list(
  title = "File Size (kb)"
)
y <- list(
  title = "Time Taken (ms)"
)

p <- plot_ly(
  cp1, x = ~Size, y = ~time,
  alpha = 0.7,
  type = 'scatter',
  name = "CP1",
  color = I('#3E6D8F')
) %>%
  layout(xaxis = x, yaxis = y) %>%
  add_markers(
    data = cp2, x = ~cp2$Size, y = ~cp2$time,
    name = "CP2",
    color = I("#7BD2B4")) %>%
  add_lines(
    x = ~cp1$Size, y = fitted(cp1.linearModel),
    name = "CP1 Regression Line",
    color = I("#3D477C"),
    alpha = 1
  ) %>%
  add_lines(
    x = ~cp2$Size, y = fitted(cp2.linearModel),
    name = "CP2 Regression Line",
    color = I("#1e7a58"),
    alpha = 1
  ) 

p 
