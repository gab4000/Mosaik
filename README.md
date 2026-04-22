> [!WARNING]
> README written by Github Copilot, but proofread and validated by me

# Mosaik

Mosaik is a graphical puzzle program developed in Java using the OpenGL API. It features a grid-based interface where the cells are outlined by borders of different colors, following specific rules that allow the puzzle to be solved.

## Overview

The program features a defined grid of cells, where each cell consists of four triangular borders (top, right, bottom, left). Users can activate and color these borders using four colors: blue, red, yellow, and green. The fundamental rule is that, within each cell, all activated borders must have unique colors: no two borders within the same cell may share the same color.

## Gameplay

### Controls
- **Left Mouse Click**: Activate and color the border under the cursor with the current selected color.
- **Right Mouse Click**: Deactivate the border under the cursor, resetting it to the default gray color.
- **Middle Mouse Drag**: Pan the camera to navigate the grid.
- **Mouse Scroll**: Zoom in and out of the grid.
- **Arrow Keys**:
  - Up Arrow: Select Blue color
  - Down Arrow: Select Red color
  - Right Arrow: Select Yellow color
  - Left Arrow: Select Green color
- **Escape**: Exit the program

### Rules
- Borders can be activated (colored) or deactivated (gray).
- In any single cell, activated borders must all have different colors.
- The program enforces these rules during interaction, preventing invalid moves.

### Visual Elements
- **Grid**: A 3x3 arrangement of cells, each with four borders.
- **Cursor**: A colored triangle indicating the current selected color and mouse position.
- **Borders**: Triangular segments that can be colored or gray.
- **Background**: Black background for contrast.

## Technical Details

### Dependencies
- **[XernasDev/Photon](https://github.com/XernasDev/Photon)**: Custom graphic engine for rendering and window management.
- **[XernasDev/Microscope](https://github.com/XernasDev/Microscope)**: Utility library for resource handling.
- **JOML**: For vector and matrix operations.

### Architecture
- **Main.java**: Entry point, initializes the Photon API and starts the program loop.
- **Mosaik.java**: Main program class handling the render loop, input processing, and camera controls.
- **Grid.java**: Manages the grid structure, cells, and border logic.
- **Models**: TriangleModel and CursorModel define the 3D models for borders and cursor.
- **Shaders**: OpenGL shaders for rendering (shader.vert and shader.frag).

### Rendering
The program uses the Photon library, which simplifies rendering, which is performed using OpenGL 4.5. Each border is a triangle rendered with a custom shader, supporting transformations, projections, and color uniforms.

### Validation
The program includes validation logic to ensure moves adhere to the puzzle rules, checking cell states and neighbor interactions.

## Executables
Executables will be available in few days, but you can build and run the project using the instructions below.

## Building and Running

1. Ensure Java 25 and Maven are installed.
2. Clone the repository and navigate to the project directory.
3. Run `mvn clean compile` to build the project.
4. Execute the main class: `java -cp target/classes fr.gab400.mosaik.Main`

Note: The project uses preview features of Java 25, so ensure your JVM supports it.
