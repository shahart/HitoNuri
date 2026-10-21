Software title: Hitori and Nurikabe
version: 1.0 (~2008)

Category: games\puzzles

size: 32KB

Min Req: OS 3.5

https://palmdb.net/app/hitori-nurikabe-japanese

### Palm's help button: ###

from my RCP file:

This is my first app for the Palm platform.

Written in Java using Jump - thanks! see also PilotJFC and WabaJump.

Hitori: Shade squares.. black so that the numbers not appear in a row or column more than once. In addition, shaded squares must... not touch each other vertically or horizontally while all un-shaded squares must create a single continuous area.

Nurikabe (or 'Islands in the Stream'): All of the black cells must be connected (But No 2x2 blocks); Island size equals to the number it contains.

Enjoy.
    
### Desc.: ###

Enjoy Hitori and Nurikabe - with 17 predefined puzzles (from time to time I'll enrich it). 

As I didn't found a small PRC for these puzzles (except using 3 PRCs for total size of 926 KB), I decided to do it by myself, using Jump2 for Java.

For info about the rules, please refer to Wikipedia: Hitori or Nurikabe. or http://shahart.github.io/HitoNuri

Features:

- The problematic squares will be in red, so you'll focus on solving the puzzle.
- Current puzzle is in the "hitoNuri" Memo. you can edit it.
  (but don't use the Solve button ":(" if you change completely this Memo)
- detect when you solve it :)

Source code is attached.


![](https://github.com/shahart/HitoNuri/blob/master/Hitori.GIF)


![](https://github.com/shahart/HitoNuri/blob/master/Nurikabe.GIF)


Would you like to play online ?
- Goto https://cloudpilot-emu.github.io/app/#/tab/emulation
- Install the provided ROM
- Drag the provided PRC

Or use an App' like https://apps.apple.com/us/app/cloudpilotemu/id6478502699

## 'Reverse engineering anything' skill

Porting from Palm to JS -> Enjoy it right here, no emulator is needed any more.

- Tap-to-mark controls, restart, previous/next, switch-game, solve, and rules
- Conflict highlighting and solved-state detection
- Keyboard shortcuts: arrows, r, s, g

The typed source is `docs/hitonuri.ts`; `docs/hitonuri.js` is its browser-ready JavaScript build.

`npx tsc && node --test docs/hitonuri.test.js`
