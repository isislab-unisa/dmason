# Changelog DMASON 3.2

### 1. Updated bower dependencies
In order to make front-end usable again, experimental library `polymer` has been udpated to version 2; while deprecated Paper layout has been dropped leaving room for App layout, `webcomponentsjs` library has been updated to 0.7.24 (version 1 doesn't currently work with some browsers).
`custom_components` folder has been extracted from `bower_components` to keep it separated from third-part bower components.

### 2 Updated front-end
JSPs, scripts and style have been updated in order to make front-end usable again by any web browser.

### 3. Updated `README.md` and `CHANGELOG.md`
Markdown files have been updated in order to be properly shown in GitHub (according to updated [GFM specs](https://github.github.com/gfm/)).

## TODO LIST
- Workers reconnection (beta version has been created) 
- Simulation Viewer for uniform field partitioning (beta version has been created) 
- Include communication with MPI in System Management 
- Ultimate JUnit Testing
- Distributed 3D Fields  

For further details, read the [README.md](https://github.com/isislab-unisa/dmason/blob/master/src/main/java/it/isislab/dmason/sim/field/network/kway/README.md) file.
