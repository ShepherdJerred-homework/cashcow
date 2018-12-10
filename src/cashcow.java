// cashcow
// Jerred Shepherd

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class cashcow {
    private static final int[] inputRowMap = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};

    public static void main(String[] args) throws FileNotFoundException {
        List<Input> inputs = getInputs();
        List<Output> outputs = solveInputs(inputs);
        printOutputs(outputs);
    }

    public static void printOutputs(List<Output> outputs) throws FileNotFoundException {
        File outputFile = new File("cashcow.out");
        PrintWriter printWriter = new PrintWriter(outputFile);
        for (Output output : outputs) {
            printWriter.println(output.toFormattedString());
            System.out.println(output);
        }
        printWriter.close();
    }

    public static Output solveInput(Input input) {
        input.moves.forEach(input.board::doMove);
        return new Output(input.board.numberOfActiveCells);
    }

    public static List<Output> solveInputs(List<Input> inputs) {
        List<Output> outputs = new ArrayList<>();
        inputs.forEach(input -> {
            Output output = solveInput(input);
            outputs.add(output);
        });
        return outputs;
    }

    public static List<Input> getInputs() throws FileNotFoundException {
        File inputFile = new File("cashcow.in");
        Scanner scanner = new Scanner(inputFile);
        List<Input> inputs = new ArrayList<>();

        while (scanner.hasNext()) {
            int numberOfTurns = scanner.nextInt();

            if (numberOfTurns == 0) {
                break;
            }

            char[][] cells = new char[12][10];
            for (int row = 0; row < 12; row++) {
                char[] rowChars = scanner.next().toCharArray();
                for (int col = 0; col < 10; col++) {
                    cells[row][col] = rowChars[col];
                }
            }
            Board board = new Board(cells);

            List<Coordinate> moves = new ArrayList<>();
            for (int i = 0; i < numberOfTurns; i++) {
                char col = scanner.next().charAt(0);
                int row = Integer.valueOf(scanner.next());
                Coordinate coordinate = new Coordinate(inputRowMap[row - 1], col);
                moves.add(coordinate);
            }

            Input input = new Input(numberOfTurns, board, moves);
            inputs.add(input);
        }

        return inputs;
    }

    public static class Output {
        final int remainingCells;

        public Output(int remainingCells) {
            this.remainingCells = remainingCells;
        }

        public String toFormattedString() {
            return String.valueOf(remainingCells);
        }

        @Override
        public String toString() {
            return "Output{" +
                    "remainingCells=" + remainingCells +
                    '}';
        }
    }

    public static class Input {
        final int numberOfTurns;
        final Board board;
        final List<Coordinate> moves;

        public Input(int numberOfTurns, Board board, List<Coordinate> moves) {
            this.numberOfTurns = numberOfTurns;
            this.board = board;
            this.moves = moves;
        }

        @Override
        public String toString() {
            return "Input{" +
                    "numberOfTurns=" + numberOfTurns +
                    ", board=" + board +
                    ", moves=" + moves +
                    '}';
        }
    }

    public static class Coordinate {
        final int row;
        final int column;

        public Coordinate(int row, int column) {
            this.row = row;
            this.column = column;
        }

        public Coordinate(int row, char column) {
            this.row = row;
            this.column = Character.toLowerCase(column) - 97;
        }

        @Override
        public String toString() {
            return "Coordinate{" +
                    "row=" + row +
                    ", column=" + column +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Coordinate that = (Coordinate) o;
            return row == that.row &&
                    column == that.column;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, column);
        }
    }

    public static class Board {
        char[][] cells;
        final int columns;
        final int rows;
        int numberOfActiveCells;

        Board(char[][] cells) {
            this.cells = cells;
            rows = cells.length;
            columns = cells[0].length;
            numberOfActiveCells = rows * columns;
        }

        boolean isColumnEmpty(int col) {
            boolean isEmpty = true;
            for (int row = 0; row < rows; row++) {
                if (cells[row][col] != ' ') {
                    isEmpty = false;
                    break;
                }
            }
            return isEmpty;
        }

        void moveColumn(int src, int dest) {
            for (int row = 0; row < rows; row++) {
                cells[row][dest] = cells[row][src];
                cells[row][src] = ' ';
            }
        }

        void shiftEmptyColumnsLeft() {
            int nextColumnToFill = -1;
            for (int col = 0; col < columns; col++) {
                if (isColumnEmpty(col)) {
                    if (nextColumnToFill == -1) {
                        nextColumnToFill = col;
                    }
                } else {
                    if (nextColumnToFill != -1) {
                        moveColumn(col, nextColumnToFill);
                        nextColumnToFill++;
                    }
                }
            }
        }

        void moveCellsDown(Set<Integer> columns) {
            columns.forEach(column -> {
                int nextRowToFill = -1;
                for (int row = rows - 1; row >= 0; row--) {
                    if (cells[row][column] == ' ') {
                        if (nextRowToFill == -1) {
                            nextRowToFill = row;
                        }
                    } else {
                        if (nextRowToFill != -1) {
                            cells[nextRowToFill][column] = cells[row][column];
                            cells[row][column] = ' ';
                            nextRowToFill--;
                        }
                    }
                }
            });
        }

        Set<Integer> setCellsToEmpty(Set<Coordinate> coordinates) {
            Set<Integer> effectedColumns = new HashSet<>();
            coordinates.forEach(coordinate -> {
                cells[coordinate.row][coordinate.column] = ' ';
                effectedColumns.add(coordinate.column);
            });
            return effectedColumns;
        }

        void doMove(Coordinate coordinate) {
            System.out.println("BEFORE");
            System.out.println(this);

            char color = cells[coordinate.row][coordinate.column];
            Set<Coordinate> adjacentCells = findAdjacentCellsOfColor(coordinate, color, new HashSet<>());

            System.out.println(coordinate);
            System.out.println("Color: " + color);

            if (adjacentCells.size() > 2) {
                Set<Integer> effectedColumns = setCellsToEmpty(adjacentCells);
                moveCellsDown(effectedColumns);
                shiftEmptyColumnsLeft();
                System.out.println(adjacentCells.size());
                numberOfActiveCells -= adjacentCells.size();
            }

            System.out.println("AFTER");
            System.out.println(this);
        }

        boolean isValidCoordinate(Coordinate coordinate) {
            return coordinate.row >= 0 && coordinate.row < rows && coordinate.column >= 0 && coordinate.column < columns;
        }

        Set<Coordinate> findAdjacentCellsOfColor(Coordinate coordinate, char color, Set<Coordinate> checkedCells) {
            Set<Coordinate> adjacentCellsOfSameColor = new HashSet<>();
            checkedCells.add(coordinate);

            if (!isValidCoordinate(coordinate)) {
                return adjacentCellsOfSameColor;
            }

            char cell = cells[coordinate.row][coordinate.column];

            if (cell != ' ' && cell == color) {
                adjacentCellsOfSameColor.add(coordinate);
                List<Coordinate> adjacentCells = new ArrayList<>();
                Coordinate right = new Coordinate(coordinate.row + 1, coordinate.column);
                Coordinate left = new Coordinate(coordinate.row - 1, coordinate.column);
                Coordinate down = new Coordinate(coordinate.row, coordinate.column + 1);
                Coordinate up = new Coordinate(coordinate.row, coordinate.column - 1);

                adjacentCells.add(right);
                adjacentCells.add(left);
                adjacentCells.add(down);
                adjacentCells.add(up);

                adjacentCells.forEach(adjCell -> {
                    if (!checkedCells.contains(adjCell)) {
                        adjacentCellsOfSameColor.addAll(findAdjacentCellsOfColor(adjCell, color, checkedCells));
                    }
                });
            }

            return adjacentCellsOfSameColor;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("   ");
            for (int col = 0; col < columns; col++) {
                sb.append(col);
            }
            sb.append('\n');
            for (int row = 0; row < rows; row++) {
                if (row < 10) {
                    sb.append(row + "  ");
                } else {
                    sb.append(row + " ");
                }

                for (int col = 0; col < columns; col++) {
                    sb.append(cells[row][col]);
                }
                sb.append('\n');
            }
            return sb.toString();
        }
    }
}

