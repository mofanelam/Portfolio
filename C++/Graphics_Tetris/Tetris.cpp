#ifdef __APPLE__
#include <GLUT/glut.h>
#else
#include <GL/glut.h>
#endif

#include <stddef.h>
#include <stdlib.h>
#include <iostream>
#include <vector>
#include <ctime>
#include <sstream>
#include <string>
#include <cmath>

// Define functions.
void init();
void draw_3Dpreview();

//Define key numbers.
const int gridRows = 20;
const int gridColumns = 10;

//Define the types of the tetris.
int type = 0;
int nextType = 0;
int rotation = 0;

//Define a value which will be used to calculate the glTranslatef needed.
int score_offset = 1;

// All rotations of a tetris blocks
static int tetrisType[7][4][4][4] = {
	// "L1"
	{{{1, 0, 0, 0},
	 {1, 1, 1, 0},
	 {0, 0, 0, 0},
	 {0, 0, 0, 0}},

	 {{0, 1, 1, 0},
 	 {0, 1, 0, 0},
 	 {0, 1, 0, 0},
 	 {0, 0, 0, 0}},

	 {{0, 0, 0, 0},
 	 {1, 1, 1, 0},
 	 {0, 0, 1, 0},
 	 {0, 0, 0, 0}},

	 {{0, 1, 0, 0},
 	 {0, 1, 0, 0},
 	 {1, 1, 0, 0},
 	 {0, 0, 0, 0}}},

	 // "L2"
 	{{{0, 0, 1, 0},
 	 {1, 1, 1, 0},
 	 {0, 0, 0, 0},
 	 {0, 0, 0, 0}},

 	 {{0, 1, 0, 0},
  	 {0, 1, 0, 0},
  	 {0, 1, 1, 0},
  	 {0, 0, 0, 0}},

 	 {{0, 0, 0, 0},
  	 {1, 1, 1, 0},
  	 {1, 0, 0, 0},
  	 {0, 0, 0, 0}},

 	 {{1, 1, 0, 0},
  	 {0, 1, 0, 0},
  	 {0, 1, 0, 0},
  	 {0, 0, 0, 0}}},

	// "Straight"
	{{{0, 0, 0, 0},
	{1, 1, 1, 1},
	{0, 0, 0, 0},
	{0, 0, 0, 0}},

	{{0, 0, 1, 0},
	{0, 0, 1, 0},
	{0, 0, 1, 0},
	{0, 0, 1, 0}},

	{{0, 0, 0, 0},
	{0, 0, 0, 0},
	{1, 1, 1, 1},
	{0, 0, 0, 0}},

	{{0, 1, 0, 0},
	{0, 1, 0, 0},
	{0, 1, 0, 0},
	{0, 1, 0, 0}}},

	// "Tud"
	{{{0, 1, 0, 0},
	{1, 1, 1, 0},
	{0, 0, 0, 0},
	{0, 0, 0, 0}},

	{{0, 1, 0, 0},
	{0, 1, 1, 0},
	{0, 1, 0, 0},
	{0, 0, 0, 0}},

	{{0, 0, 0, 0},
	{1, 1, 1, 0},
	{0, 1, 0, 0},
	{0, 0, 0, 0}},

	{{0, 1, 0, 0},
	{1, 1, 0, 0},
	{0, 1, 0, 0},
	{0, 0, 0, 0}}},

	// "Fong"
	{{{0, 1, 1, 0},
	{0, 1, 1, 0},
	{0, 0, 0, 0},
	{0, 0, 0, 0}},

	{{0, 1, 1, 0},
	{0, 1, 1, 0},
	{0, 0, 0, 0},
	{0, 0, 0, 0}},

	{{0, 1, 1, 0},
	{0, 1, 1, 0},
	{0, 0, 0, 0},
	{0, 0, 0, 0}},

	{{0, 1, 1, 0},
	{0, 1, 1, 0},
	{0, 0, 0, 0},
	{0, 0, 0, 0}}},
	// "Z1"
	{{{0, 1, 1, 0},
	{1, 1, 0, 0},
	{0, 0, 0, 0},
	{0, 0, 0, 0}},

	{{0, 1, 0, 0},
	{0, 1, 1, 0},
	{0, 0, 1, 0},
	{0, 0, 0, 0}},

	{{0, 0, 0, 0},
	{0, 1, 1, 0},
	{1, 1, 0, 0},
	{0, 0, 0, 0}},

	{{1, 0, 0, 0},
	{1, 1, 0, 0},
	{0, 1, 0, 0},
	{0, 0, 0, 0}}},

	// "Z2"
	{{{1, 1, 0, 0},
	{0, 1, 1, 0},
	{0, 0, 0, 0},
	{0, 0, 0, 0}},

	{{0, 0, 1, 0},
	{0, 1, 1, 0},
	{0, 1, 0, 0},
	{0, 0, 0, 0}},

	{{0, 0, 0, 0},
	{1, 1, 0, 0},
	{0, 1, 1, 0},
	{0, 0, 0, 0}},

	{{0, 1, 0, 0},
	{1, 1, 0, 0},
	{1, 0, 0, 0},
	{0, 0, 0, 0}}}
};

//Define colors for each tetris type
static float colorType[7][3] = {
	{0.1f, 0.6f, 1.0f},
	{0.9f, 0.6f, 0.2f},
	{0.7f, 0.4f, 1.0f},
	{0.8f, 0.9f, 0.0f},
	{0.9f, 0.0f, 0.7f},
	{0.9f, 0.0f, 0.2f},
	{0.1f, 0.9f, 0.3f}
};

//Define the grid
static int gridCells[gridRows][gridColumns];

// Used in check_surroundings() and check_rotate().
static int potentialRow;
static int potentialCol;

// Define the next rotation so it can be used in check_rotate().
static int potentialRotation = 0;

//Define the status of each row (if filled or not)
static bool rowFilled[gridRows];

// Define the score.
static int score = 0;

// Define the background color.
static float backgroundColor[3] = {0.5f, 0.5f, 0.5f};

// Define the Initial coordinate.
float g_Xcoordinate = 4.0f;
float g_Ycoordinate = 20.0f;

// Used for detecting collision in check_rotate() and check_surroundings().
static float potentialTopLeft[2] = {0.0f, 0.0f};

//Define a step
const float g_move_step = 1.0f;

// Initial difficulty.
static int difficulty = 1;
// Initial falling speed.
float delay = 1000;

size_t g_the_cube = 0;

//Draw the cube.
size_t make_cube()
{
	static float vertices[8][3] =
		{
			{-0.5f, -0.5f, -0.5f}, // front
			{0.5f, -0.5f, -0.5f},
			{0.5f, 0.5f, -0.5f},
			{-0.5f, 0.5f, -0.5f},
			{-0.5f, -0.5f, 0.5f}, // back
			{0.5f, -0.5f, 0.5f},
			{0.5f, 0.5f, 0.5f},
			{-0.5f, 0.5f, 0.5f}
		};

	// indices into verices
	static size_t faces[6][4] =
		{
			{0, 1, 2, 3},  // front
			{5, 4, 7, 6},  // back
			{4, 0, 3, 7},  // left
			{1, 5, 6, 2},  // right
			{4, 5, 1, 0},  // bottom
			{3, 2, 6, 7}   // top
		};

	// compile into a display list
	size_t handle = glGenLists(1);

	glNewList(handle, GL_COMPILE);
	glBegin(GL_QUADS);
	for (size_t f=0;f<6;f++) // for each face
	for (size_t v=0;v<4;v++)
		glVertex3fv(vertices[faces[f][v]]);
	glEnd();
	glEndList();

	return handle;
}

// Draw the wire(lines) for each cube.
void draw_lines(int flag)
{
	static float vertices[8][3] =
		{
			{-0.5f, -0.5f, -0.5f}, // front
			{0.5f, -0.5f, -0.5f},
			{0.5f, 0.5f, -0.5f},
			{-0.5f, 0.5f, -0.5f},
			{-0.5f, -0.5f, 0.5f}, // back
			{0.5f, -0.5f, 0.5f},
			{0.5f, 0.5f, 0.5f},
			{-0.5f, 0.5f, 0.5f}
		};

	// indices into verices
	static size_t faces[6][4] =
		{
			{0, 1, 2, 3},  // front
			{5, 4, 7, 6},  // back
			{4, 0, 3, 7},  // left
			{1, 5, 6, 2},  // right
			{4, 5, 1, 0},  // bottom
			{3, 2, 6, 7}   // top
		};

	if(flag == 1){
		glColor3f(0.0f,0.0f,0.0f);
	}else if(flag == 0) {
		glColor3f(1.0f,1.0f,1.0f);
	}

	for (size_t f=0;f<6;f++) // for each face
	{
		glBegin(GL_LINE_LOOP);
		for (size_t v=0;v<4;v++)
			glVertex3fv(vertices[faces[f][v]]);
		glEnd();
	}
}

// Function to draw the cube with lines.
void draw_cube(float colors[],int flag) {
	glColor3f(colors[0],colors[1],colors[2]);
	glCallList(g_the_cube);
	draw_lines(flag);
}


// Function to draw the text.
void draw_text(const char* text)
{
	size_t len = strlen(text);
	for (size_t i=0;i<len;i++)
		glutStrokeCharacter(GLUT_STROKE_ROMAN, text[i]);
}

// Draws the gameover Prompt.
void draw_gameOver_prompt()
{
	static float vertex[4][2] =
	{
			{0.0f, 0.0f},
			{1.0f, 0.0f},
			{1.0f, 0.5f},
			{0.0f, 0.5f}
	};
	glLineWidth(1.0f);
	glColor3f(0.254f, 0.267f, 0.254f);
	glBegin(GL_QUADS);
		for (size_t i = 0; i < 4; i++)
			glVertex2fv(vertex[i]);
	glEnd();
}

// Function used to convert integer to string.
std::string tostr (int x)
{
    std::stringstream str;
    str << x;
    return str.str();
}

// Draw the score text and convert score (int) into char*.
void draw_score()
{
	glColor3f(1.0f, 1.0f, 1.0f);
	draw_text("Score: ");
	char const* pchar = tostr(score).c_str();
	draw_text(pchar);
}

// Return a random number for randomizing tetris type
int random_num()
{
	return (rand() % 7) ;
}


void draw_grid()
{
	glTranslatef(15.0f, 13.0f, 0.0f);
	draw_3Dpreview();
	glTranslatef(-15.0f, -13.0f, 0.0f);

	//Colors
	float colors [3];

	//Draw the backgound with grey colour.
	glPushMatrix();
		for(int i = 0; i < gridRows; i++)
		{
			for(int j = 0; j < gridColumns; j++)
			{
				if(gridCells[i][j] == 0)
				draw_cube(backgroundColor, 0);
				glTranslatef(1.0f, 0.0f, 0.0f);
			}
			glTranslatef(-10.0f, 0.0f, 0.0f);
			glTranslatef(0.0f, 1.0f, 0.0f);
		}
	glPopMatrix();

	glPushMatrix();
		for(int i = 0; i < gridRows; i++)
		{
			for(int j = 0; j < gridColumns; j++)
			{
				switch(gridCells[i][j])
				{
					case 1:
						colors[0] = colorType[0][0];
						colors[1] = colorType[0][1];
						colors[2] = colorType[0][2];
						break;
					case 2:
						colors[0] = colorType[1][0];
						colors[1] = colorType[1][1];
						colors[2] = colorType[1][2];
						break;
					case 3:
						colors[0] = colorType[2][0];
						colors[1] = colorType[2][1];
						colors[2] = colorType[2][2];
						break;
					case 4:
						colors[0] = colorType[3][0];
						colors[1] = colorType[3][1];
						colors[2] = colorType[3][2];
						break;
					case 5:
						colors[0] = colorType[4][0];
						colors[1] = colorType[4][1];
						colors[2] = colorType[4][2];
						break;
					case 6:
						colors[0] = colorType[5][0];
						colors[1] = colorType[5][1];
						colors[2] = colorType[5][2];
						break;
					case 7:
						colors[0] = colorType[6][0];
						colors[1] = colorType[6][1];
						colors[2] = colorType[6][2];
						break;
					default:
						colors[0] = backgroundColor[0];
						colors[1] = backgroundColor[1];
						colors[2] = backgroundColor[2];
						break;

				}
				if(gridCells[i][j] != 0)
				{
					draw_cube(colors, 1);
				}

				glTranslatef(1.0f, 0.0f, 0.0f);
			}
			glTranslatef(-10.0f, 0.0f, 0.0f);
			glTranslatef(0.0f, 1.0f, 0.0f);
		}
	glPopMatrix();

}

// Draw the controllable block.
void draw_controllable_block()
{
	// Fetch the color with the type.
	float color[3] = {colorType[type][0], colorType[type][1], colorType[type][2]};
	glPushMatrix();
		//Compensate the bottomup drawing method.
		glTranslatef(0.0f , -3.0f, 0.0f);
		for(int i = 3; i > -1; i--) {
			for(int j = 0; j < 4; j++) {
				if(tetrisType[type][rotation][i][j] == 1) {
					draw_cube(color, 1);
				}
				glTranslatef(1.0f, 0.0f, 0.0f);
			}
			glTranslatef(-4.0f, 0.0f, 0.0f);
			glTranslatef(0.0f, 1.0f, 0.0f);
		}
	glPopMatrix();
}


// When the game is over, this function will be called and draw a end prompt.
void gameover()
{

	glScalef(10.0f, 10.0f, 1.0f);
	draw_gameOver_prompt();

	glPushMatrix();
		glTranslatef(0.05f, 0.37f, 0.0f);
		glScalef(0.001f, 0.001f, 1.0f);
		glColor3f(1.0f, 1.0f, 1.0f);
		draw_text("Game Over!");
		glTranslatef(-750.0f, -200.0f, 0.0f);
		draw_score();
		glTranslatef(-550.0f, -100.0f, 0.0f);

		//Prevent the overflow when calculating log10 of the score to calculate glTranslatef needed.
		if(score != 0) {
			score_offset = log10(score);
		} else {
			score_offset = 0;
		}

		//Since each digit of score gives a different length of text,
		//the glTranslatef needs to be calculated.

		glTranslatef(-75*score_offset, 0.0f, 0.0f);
		glScalef(0.3f, 0.3f, 1.0f);
		draw_text("Please press ' r ' or ' R ' to restart.");

	glPopMatrix();

}

// Call to check if the game is over each time.
bool check_gameover()
{

	for(int i = 0; i < gridColumns; i++)
	{
		// If the highest-1 row consist of something when a new block is formed.
		if(gridCells[gridRows-1][i] != 0) {
			gameover();
			return true;
		}
	}
	return false;
}

// Assign a type for the next block.
void new_block()
{
	nextType = random_num();
}

// Reset the coordinate and rotation type of the next controllable tetris block.
void next_block()
{
	g_Xcoordinate = 4.0f;
	g_Ycoordinate = 20.0f;
	rotation = 0;
	type = nextType;
	draw_controllable_block();
}

// Draw the piece look-ahead in 3D.
void draw_3Dpreview()
{
	float color[3] = {colorType[nextType][0], colorType[nextType][1], colorType[nextType][2]};
	glPushMatrix();

		//Draw a backgrounded grid.
		for (int i = 3; i > -1; i--) {
			for(size_t j = 0; j < 4; j++) {
				if(tetrisType[nextType][0][i][j] == 0)
				{
					draw_cube(backgroundColor, 0);
				}
				glTranslatef(1.0f, 0.0f, 0.0f);
			}
			glTranslatef(-4.0f, 0.0f, 0.0f);
			glTranslatef(0.0f, 1.0f, 0.0f);
		}
	glPopMatrix();

	glPushMatrix();

		//Draw the colored ones.
		for (int i = 3; i > -1; i--) {
			for(size_t j = 0; j < 4; j++) {
				if(tetrisType[nextType][0][i][j] == 1)
				{
					draw_cube(color, 1);
				}
				glTranslatef(1.0f, 0.0f, 0.0f);
			}
			glTranslatef(-4.0f, 0.0f, 0.0f);
			glTranslatef(0.0f, 1.0f, 0.0f);
		}

	glPopMatrix();
}

// Check if the line is full.
void check_lines(int row)
{
	rowFilled[row] = true;
	for(int i = 0; i < gridColumns; i++)
	{
		if(gridCells[row][i] == 0)
		{
			rowFilled[row] = false;
		}
	}
	if(rowFilled[row])
	{
		score += 100;
		for(int j = 0; j < gridColumns; j++) {
			gridCells[row][j] = 0;
		}
		for(int i = row; i < gridRows; i++) {
			for(int j = 0; j < gridColumns; j++) {
				if((i+1) < gridRows)
				gridCells[i][j] = gridCells[i+1][j];
			}
		}
	}
}

// Record the block into gridCells and call check_lines() to check if there is any lines.
void record_block()
{
	for(int r = 0; r < 4; r++) {
		for(int c = 0; c < 4; c++) {
			if(tetrisType[type][rotation][r][c] != 0){
				potentialRow = g_Ycoordinate - r;
				potentialCol = c + g_Xcoordinate;
				gridCells[potentialRow][potentialCol] = type + 1;
				check_lines(potentialRow);
			}
		}
	}
}

//If flag = 1 means that it's going down, flag 2 = to the left and 3 = to the right
// Checks and return boolean value so the controllable block might go left or right or down.
bool check_surroundings(int flag)
{
	switch (flag) {
		case 1:
			potentialTopLeft[0] = g_Xcoordinate;
			potentialTopLeft[1] = g_Ycoordinate - g_move_step;
			break;
		case 2:
			potentialTopLeft[0] = g_Xcoordinate - g_move_step;
			potentialTopLeft[1] = g_Ycoordinate;
			break;
		case 3:
			potentialTopLeft[0] = g_Xcoordinate + g_move_step;
			potentialTopLeft[1] = g_Ycoordinate;
			break;
		default:
			break;
	}
	//Borders
	for(int row = 0; row < 4; row++)
	{
		for(int col = 0; col < 4; col++)
		{
			potentialCol = potentialTopLeft[0] + col;
			potentialRow = potentialTopLeft[1] - row;

			if(tetrisType[type][rotation][row][col] != 0)
			{
				//Reaching the bottom
				if (potentialTopLeft[1] - row < 0)
				{
					//Call a function so a new tetris can be generated.
					if(flag == 1)
					{
						if(!check_gameover())
						{
							record_block();
							next_block();
							new_block();
						}
					}
                	return false;
				}

				//Going out of the grid to the left
				if(potentialTopLeft[0] + col < 0)
					return false;

				//Going out of the grid to the right
				if(potentialTopLeft[0] + col >= 10)
					return false;

				if (gridCells[potentialRow][potentialCol] != 0) {
					if(flag == 1)
					{
						if(!check_gameover())
						{
							record_block();
							next_block();
							new_block();
						}
					}
					return false;
				}
			}
		}

	}
	return true;
}

// Check if rotation is possible.
bool check_rotate()
{
	for(int row = 0; row < 4; row++)
	{
		for(int col = 0; col < 4; col++)
		{
			potentialCol = potentialTopLeft[0] + col;
			potentialRow = potentialTopLeft[1] - row;

			if(tetrisType[type][potentialRotation][row][col] != 0)
			{
				//Reaching the bottom
				if (g_Ycoordinate - row < 0)
                	return false;

				//Going out of the grid to the left
				if(g_Xcoordinate + col < 0)
					return false;

				//Going out of the grid to the right
				if(g_Xcoordinate + col >= 10)
					return false;

				if (gridCells[potentialRow][potentialCol] != 0) {
					return false;
				}
			}
		}

	}
	return true;
}

// A delayed event which calls itself to simulate the automatic drop of a tetris block.
void autodown(int value)
{
	potentialTopLeft[0] = g_Xcoordinate;
	potentialTopLeft[1] = g_Ycoordinate - g_move_step;

	// Check if there is anything below the controllable block.
	if(check_surroundings(1)){
		g_Ycoordinate -= g_move_step;
	}

	//Calculate the speed of the auto drop.
	delay = 1000/difficulty;
    glutTimerFunc(delay, autodown, 0);
	glutPostRedisplay();
}


void display()
{
	glClear(GL_COLOR_BUFFER_BIT);
	glColor3f(1.0f, 1.0f, 1.0f);
	// position and orient camera
	glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();

	//Add perspective change maybe?
	gluLookAt(1, 1, 2.5, // eye position
			  0, 0, 0, // reference point
			  0, 1, 0  // up vector
		);

	glPushMatrix();
		glTranslatef(0.0f, -1.5f, 0.0f);
		glTranslatef(-1.0f, 0.0f, 0.0f);
		glScalef(0.15, 0.15, 0.1f);
		draw_grid();
		glTranslatef(g_Xcoordinate, g_Ycoordinate, 0.0f);
		draw_controllable_block();
		glTranslatef(-g_Xcoordinate, -g_Ycoordinate, 0.0f);
		glTranslatef(-1.5f , 8.0f, 0.0f);
		glScalef(1.5f, 1.5f, 1.0f);
		check_gameover();
	glPopMatrix(); // done with stack

	glutSwapBuffers();
}


void keyboard(unsigned char key, int, int)
{
	switch (key)
	{
		case 'q': exit(1); // quit!

		// Spacebar makes the controllable block falls to the bottom instantly.
		case ' ':
			while(check_surroundings(1))
			{
				// Add the score by 1 each time when it falls a step.
				g_Ycoordinate -= g_move_step;
				score += 1;
			}
			break;

		// Levels of difficulty.
		case '1': difficulty = 1; break;
		case '2': difficulty = 2; break;
		case '3': difficulty = 3; break;
		case '4': difficulty = 4; break;
		case '5': difficulty = 5; break;
		case '6': difficulty = 6; break;
		case '7': difficulty = 7; break;
		case '8': difficulty = 8; break;
		case '9': difficulty = 9; break;

		// Press r or R to restart the game.
		case 'r':
		case 'R': init();
			break;

		default:
			break;
	}

	glutPostRedisplay(); // force a redraw
}


// any special key pressed like arrow keys
void special(int key, int, int)
{
	// handle special keys
	switch (key)
	{
		// Check if the area to the left of the controllable is viable
		case GLUT_KEY_LEFT:
			if(check_surroundings(2))
			g_Xcoordinate -= g_move_step;
			break;
		// Check if the area to the right of the controllable is viable
		case GLUT_KEY_RIGHT:
			if(check_surroundings(3))
			g_Xcoordinate += g_move_step;
			break;
		// Check if it is rotatable before rotating and reset it to 0 if rotated 3 times already.
		case GLUT_KEY_UP:
			potentialRotation = rotation + 1;
			if(check_rotate())
			{

				if(rotation < 3) {
					rotation++;
				}
				else {
					rotation = 0;
				}
			}
			break;
		// Check and lower the controllable block by 1 row.
		case GLUT_KEY_DOWN:
			if(check_surroundings(1)){
				g_Ycoordinate -= g_move_step;
				score += 1;
			}

			break;
	}

	glutPostRedisplay(); // force a redraw
}

// Reset the game
void init()
{
	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	// Randomize the random function by providing a time seed.
	srand(time(NULL));

	glOrtho(-2.0, 2.0, -2.0, 2.0, -4.0, 4.0);

	// construct the cube to the handle list which will be used later.
	g_the_cube = make_cube();

	//Set each cell to not filled.
	for(size_t i = 0; i < 20; i++)
	{
		for(size_t j = 0; j < 10; j++)
		{
			gridCells[i][j] = 0;
		}
		// Set the row to be false.
		rowFilled[i] = false;
	}
	// Reset the score and types.
	score = 0;
	type = random_num();
	new_block();
}

int main(int argc, char* argv[])
{
	glutInit(&argc, argv);
	glutInitDisplayMode(GLUT_DOUBLE|GLUT_RGBA);
	glutInitWindowSize(720, 720);
	glutInitWindowPosition(50, 50);
	glutCreateWindow("Pseudo-3D Tetris");
	glutDisplayFunc(display);

	glutTimerFunc(delay, autodown, 0);
	// handlers for keyboard input
	glutKeyboardFunc(keyboard);
	glutSpecialFunc(special);

	init();
	glutMainLoop();

	return 0;
}
