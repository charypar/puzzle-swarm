# Puzzle Swarm

Puzzle Swarm is an experimental Monte Carlo simulation model of a puzzlehunt game.

## Install & Run

Puzzle Swarm is written in Java and built using NetBeans IDE. You should be able to just open the project and build and run. No standalone packages are available. Experimental, remember?

## About

This was originally a school project and an experiment serving three purposes: 1) trying to model and simulate a puzzle hunt game, 2) implementing a Monte Carlo simulation and 3) building a simple MVC architecture desktop GUI app in Java using Swing and seeing what happens.

I figured it might be useful to someone for one of these reasons. I also want to keep it somewhere.

### Right, what is a puzzlehunt?

In case you're not familiar with them (and you probably aren't), puzzlehunt is a race where teams compete against each other in solving various puzzles. The specific
kind I had in mind here is a Czech variation which usually happens outdoor and mostly at night. The relevant bit for modeling is that the whole game consists of obtaining a puzzle, figuring out the code used and solving it to get a location of the next puzzle, i.e. repeated phases of deciphering and advancing to the next checkpoint.

To people organizing these games (e.g. me) it is important to know when a given puzzle copies have to be in their place for the leading team and also when there is no need to leave the remaining puzzles there because everyone either already got it, or gave up. And in general, it can't hurt to be able to predict what the distribution of teams over checkpoints might be at any given time.

### Got it, how do I model it? And what is Monte Carlo?

Good question! Since the times involved are random, it seems reasonable to use some kind of stochastic modeling. And essentially, we can model anything about the game if we can model the time it takes to solve a puzzle and move to the next one. That is random, but follows a certain distribution. The difficulty of the puzzles and travel times are hidden in its parameters. Chain a bunch of these in a row and you've got a single run of a game for a single team. Do it *n* times (for *n* teams in a game) and you have a single randomly generated puzzlehunt.

To get any sensible results, we need some statistical measures of these random realizations. For that we turn to the [Monte Carlo method](http://en.wikipedia.org/wiki/Monte_Carlo_method): we do this whole thing thousands of times and compute means, medians, standard errors, etc. of anything you're interested in.

### "A certain distribution", you say...

That's right. Specifically, the [Weibull distribution](http://en.wikipedia.org/wiki/Weibull_distribution). Since it only gives positive numbers, which we need for times spent on a puzzle and is used in modeling reliability, survival times (of the puzzle in this case), etc. it sounds like a good guess. If only we could validate it somehow... Luckily, we can.

For the reasons given above, organizers of outdoor puzzle hunts don't only want to estimate how the game WILL evolve, they want to know how it actually IS evolving, as it's happening, live. For that, they use SMS systems that receive text messages the teams send from each checkpoint and track the times. Thy are also fun to watch. The only drawback is that the two times (decoding and travel) are not usually available separately. But we can try anyway, worst case it will predict with a larger variance because of the fairly constant travel time skewing the data. We'll get to bigger practical problems later.

Using that dataset, I tested whether the times followed a Weibull distribution using the chi-squared goodness-of-fit test and wasn't able to reject the hypothesis of the distributions being the same (on a significance level 0.05). Good enough.

### So can I go ahead and use it for my puzzlehunt?

Yes you can! But there is a catch: you'll need to set the two parameters of the Weibull distribution for each of the checkpoints of your game. To do that, you'd ideally fit the distribution to some data from testing, but you probably won't have enough. So the best you can probably do is a) guess, b) take a similar puzzle from a different game you can get time data for and set something similar, i.e. guess.

On top of that, analysis of existing data isn't part of the program (but feel free to implement it, pull requests are welcome), and especially of online data.

## License

Puzzle Swarm is released under the [MIT License](http://www.opensource.org/licenses/MIT).
