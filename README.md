# roverimages
**Update: now supports fetching images from new Mars Perseverance rover**

A command-line tool for downloading image data gathered by NASA's Curiosity, Opportunity, Spirit and Perseverance rovers.

Note that you will need an API key to use this, you can get one here: https://api.nasa.gov

```
usage: rovfetch <sol> <rover> <directory>
```
       
sol: the martian sol to fetch images from

rover: one of four Mars rovers; curiosity, opportunity, spirit or perseverance

directory: the resource directory to save JSON and the fetched images

## example
the following would fetch all curiosity rover images from sol 19 saving them into ./res:
```
> rovfetch 19 curiosity .
```

Blog post [here](https://dyluc.github.io/2020/12/07/rover-images.html).
