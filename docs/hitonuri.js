(() => {
  // Puzzle catalogue and solutions transcribed from the original Palm PRC.
  const hitoriPuzzles = [
    '-1 1 -1 6 -5|1 6 2 4 5|2 -2 5 -2 4|6 4 -2 1 -6|-1 5 1 3 2',
    '2 5 -5 4 -5|-2 4 1 3 6|3 -3 4 2 -6|5 2 -1 1 3|-2 6 2 5 1',
    '-1 3 -3 1 -3 2|1 6 7 2 3 4|5 4 -7 3 -5 6|-1 1 3 4 6 5|2 5 -2 6 -3 1|-1 2 1 5 4 7',
    '-3 3 -6 1 -3 4|3 2 6 7 1 5|-1 1 -1 3 -1 2|1 6 3 5 4 -1|4 -4 1 -1 3 6|-3 7 5 6 2 3',
    '-5 2 -1 1 6 5 3|5 8 1 4 -6 2 -5|1 -1 3 -1 8 4 2|-4 4 -4 8 -4 3 -3|2 5 8 3 1 7 4|-5 1 -1 5 -6 8 7|4 7 5 2 3 1 6',
    '-3 2 -2 5 -7 4 -8|3 5 2 6 7 1 8|-6 6 -6 7 -6 5 -6|5 4 7 2 8 3 6|6 -6 5 -6 1 -6 7|-3 7 -2 8 -7 6 1|1 3 6 4 2 8 5',
    '-7 7 -7 2 -7 6 -7 3|4 6 2 5 9 7 3 8|-4 5 9 -2 6 -6 1 -3|1 2 7 8 5 3 4 9|-8 8 -2 3 -9 9 -3 1|6 4 8 1 7 2 9 5|-4 3 -3 9 -3 8 -3 4|5 9 4 7 3 1 2 6'
  ];

  const nurikabePuzzles = [
    '6 0 0 * 1|0 0 0 * *|* * * *|0 4 * 0 4|0 0 * * 0',
    '* * * * 0|0 * 0 * 3|2 * 0 * 0|* * 3 * *|0 2 * * 1',
    '* 3 0 0 * 1 *|* * * * * * *|2 0 * 1 * 0 0|* * * * * * 0|* 1 * 0 2 * 0|* * 2 * * * 0|1 * 0 * 1 * 6',
    '5 * 1 * 0 0 0|0 * * * * 0 0|0 0 0 * 1 * 6|* * * * * * *|2 * 4 0 0 * 0|0 * 0 * * * 0|* * * 0 2 * 3',
    '* * 4 0 0 0 * 5|* 1 * * * * * 0|* * 2 0 * 0 * 0|0 * * * * 2 * 0|0 0 4 * 0 * * 0|* * * * 0 3 * *|0 * 0 * * * 1 *|2 * 0 0 0 5 * *',
    '0 2 * 0 0 0 0 0|* * * * * * * 6|* 3 0 * 0 3 * *|1 * 0 * 0 * 0 0|* * * * * * * 3|* 0 2 * 0 0 3 *|2 * * * * * * *|0 * 0 0 0 0 6 0',
    '0 5 * * 3 0 * 0 0|0 * 8 * 0 * * 5|0 * 0 * * * 2 *|0 * 0 0 0 * 0 * *|* * * * 0 * * * 0|* 0 0 * 0 * 0 * 0|* * 3 * 0 * 0 * 0|* 3 * * * * 3 *|* 0 0 * 1 * * 6',
    '3 0 0 * * * * * *|* * * * 0 * 0 0 *|1 * 0 * 2 * * 3 *|* * 2 * * 2 * * 0|0 3 * 0 * 0 * 3 0|0 * * 2 * * 2 * *|* 3 * * 1 * 0 * 1|* 0 0 * * * * * *|* * * * 0 0 0 0 5',
    '0 0 0 0 * * * * * *|5 * * * 5 0 0 0 * 2|* 3 * 4 * * * 0 * 0|* 0 * 0 0 0 * * * *|* 0 * * * * 1 * 1 *|* * * 2 0 * * * * 3|3 * 1 * * * 0 0 * 0|0 0 * 0 0 3 * 3 *|* * * * * * * * * *|4 0 0 0 * 0 0 0 0 5',
    '0 2 * 0 0 0 0 8 * *|* * * 0 * * * * 5 *|0 * 0 0 * 0 0 0 0 *|0 * * * * * * * * *|3 * 0 0 0 * 0 0 4 *|* 5 0 * * * 0 * * 3|* * * * 0 * * * 0 0|* 0 0 0 0 * 0 * * *|* 6 * * * * 0 * 0 *|* * 7 0 0 0 0 * 2 *'
  ];

  const byId = (id) => {
    const element = document.getElementById(id);
    if (!element) throw new Error(`Missing required element: ${id}`);
    return element;
  };
  const parseHitori = (puzzle) => puzzle.split('|').map((row) => row.split(' ').map(Number));
  const parseNurikabe = (puzzle) => {
    const rows = puzzle.split('|').map((row) => row.split(' ').map((cell) => (
      cell === '*' ? 'black' : Number(cell) ? 'clue' : 'empty'
    )));
    const gridSize = rows[0].length;
    return rows.map((row) => row.concat(Array(Math.max(0, gridSize - row.length)).fill('empty')));
  };
  const parseNurikabeClues = (puzzle) => puzzle.split('|').map((row) => row.split(' ').map((cell) => Number(cell) || 0));

  let gameType = 'hitori';
  let puzzleIndex = 0;
  let puzzle;
  let state;
  let nurikabeClues = [];

  function loadPuzzle(offset = 0) {
    const puzzles = gameType === 'hitori' ? hitoriPuzzles : nurikabePuzzles;
    puzzleIndex = (puzzleIndex + offset + puzzles.length) % puzzles.length;
    puzzle = gameType === 'hitori' ? parseHitori(puzzles[puzzleIndex]) : parseNurikabe(puzzles[puzzleIndex]);
    nurikabeClues = gameType === 'nurikabe' ? parseNurikabeClues(nurikabePuzzles[puzzleIndex]) : [];
    resetState();
  }

  function resetState() {
    state = puzzle.map((row) => row.map((cell) => (
      gameType === 'hitori' ? 'white' : cell === 'clue' ? 'clue' : 'empty'
    )));
    render();
  }

  function hasHitoriConflict(row, column) {
    if (state[row][column] !== 'white') return false;
    const value = Math.abs(puzzle[row][column]);
    return puzzle[row].some((cell, index) => index !== column && state[row][index] === 'white' && Math.abs(cell) === value)
      || puzzle.some((cells, index) => index !== row && state[index][column] === 'white' && Math.abs(cells[column]) === value);
  }

  function hasNurikabePool(row, column) {
    if (state[row][column] !== 'black') return false;
    for (let top = Math.max(0, row - 1); top <= row; top += 1) {
      for (let left = Math.max(0, column - 1); left <= column; left += 1) {
        if ([[top, left], [top + 1, left], [top, left + 1], [top + 1, left + 1]].every(([y, x]) => state[y]?.[x] === 'black')) return true;
      }
    }
    return false;
  }

  function isSolved() {
    const solution = gameType === 'hitori'
      ? parseHitori(hitoriPuzzles[puzzleIndex]).map((row) => row.map((cell) => cell < 0 ? 'black' : 'white'))
      : parseNurikabe(nurikabePuzzles[puzzleIndex]);
    return state.every((row, y) => row.every((cell, x) => cell === solution[y][x]));
  }

  function tapCell(row, column) {
    if (gameType === 'hitori') state[row][column] = state[row][column] === 'black' ? 'white' : 'black';
    else if (state[row][column] !== 'clue') {
      const transitions = { empty: 'dot', dot: 'black', black: 'empty' };
      state[row][column] = transitions[state[row][column]];
    }
    render();
  }

  function render() {
    const board = byId('board');
    board.style.gridTemplateColumns = `repeat(${puzzle.length}, 1fr)`;
    board.innerHTML = '';
    puzzle.forEach((row, y) => row.forEach((cell, x) => {
      const button = document.createElement('button');
      button.className = `cell ${state[y][x]}`;
      button.disabled = gameType === 'nurikabe' && state[y][x] === 'clue';
      button.textContent = gameType === 'hitori' ? String(Math.abs(cell)) : state[y][x] === 'clue' ? String(nurikabeClues[y][x]) : '';
      if (gameType === 'hitori' ? hasHitoriConflict(y, x) : hasNurikabePool(y, x)) button.classList.add('conflict');
      button.addEventListener('click', () => tapCell(y, x));
      board.append(button);
    }));
    byId('title').textContent = `${gameType === 'hitori' ? 'Hitori' : 'Nurikabe'} ${puzzleIndex + 1}`;
    byId('switch').textContent = `Switch to ${gameType === 'hitori' ? 'Nurikabe' : 'Hitori'}`;
    byId('message').textContent = isSolved() ? ':) Solved!' : gameType === 'hitori' ? 'Tap a number to black it out' : 'Tap: empty → dot → sea';
    byId('message').className = `message${isSolved() ? ' win' : ''}`;
  }

  function showSolution() {
    state = gameType === 'hitori'
      ? parseHitori(hitoriPuzzles[puzzleIndex]).map((row) => row.map((cell) => cell < 0 ? 'black' : 'white'))
      : parseNurikabe(nurikabePuzzles[puzzleIndex]);
    render();
  }

  function toggleGame() { gameType = gameType === 'hitori' ? 'nurikabe' : 'hitori'; puzzleIndex = 0; loadPuzzle(); }
  byId('restart').onclick = byId('restart2').onclick = resetState;
  byId('next').onclick = byId('next2').onclick = () => loadPuzzle(1);
  byId('prev').onclick = () => loadPuzzle(-1);
  byId('solve').onclick = showSolution; byId('switch').onclick = toggleGame;
  byId('helpBtn').onclick = byId('helpBtn2').onclick = () => byId('legacy').classList.add('open');
  byId('closeLegacy').onclick = () => byId('legacy').classList.remove('open');
  byId('legacy').onclick = (event) => { if (event.target === byId('legacy')) byId('legacy').classList.remove('open'); };

  document.addEventListener('keydown', (event) => {
    if (event.key === 'ArrowRight') loadPuzzle(1); if (event.key === 'ArrowLeft') loadPuzzle(-1);
    if (event.key === 'r') resetState(); if (event.key === 's') showSolution(); if (event.key === 'g') toggleGame();
  });
  const updateClock = () => { byId('clock').textContent = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }); };
  updateClock(); setInterval(updateClock, 30000); loadPuzzle();
})();
