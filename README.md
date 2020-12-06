# roverimages
A command-line tool for downloading image data gathered by NASA's Curiosity, Opportunity, and Spirit rovers.

Note that you will need an API key to use this, you can get one here: https://api.nasa.gov

```
usage: rovfetch <sol> <rover> <directory>
```
       
sol: the martian sol to fetch images from

rover: one of three mars rovers; curiosity, opportunity or spirit

directory: the resource directory to save JSON and the fetched images

## example
the following would fetch all curiosity rover images from sol 19 saving them into ./res:
```
> rovfetch 19 curiosity .
```