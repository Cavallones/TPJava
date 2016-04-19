import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/**
 *  Classe principale, initialisation du modèle, et mise en place du contrôleur
 *  et de la vue.
 */

public class Laby
{

    public static void main(String[] args) {

	// Initialisation du schéma MVC.
	Model laby = new Model(10, 12);
	JFrame frame = new JFrame();
	Controller controller = new Controller(laby, frame);
	View view = new View(laby, frame);
	// -- Schéma MVC initialisé.

	// Configuration de la fenêtre graphique.
	frame.setTitle("Laby");
	frame.pack();
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setVisible(true);
	// -- Fenêtre graphique configurée.

	// Configuration du labyrinthe.
	// // Première section de configuration.
	 for (int i=0; i<10; i++) {
	     laby.putWall(i, 0);
	     laby.putWall(i, 11);
	 }
	 for (int j=0; j<12; j++) {
	     laby.putWall(0, j);
	     laby.putWall(9, j);
	 }
	 for (int i=0; i<7; i++) {
	     laby.putWall(i, 4);
	 }
	 for (int i=8; i<10; i++) {
	     laby.putWall(i, 4);
	 }
	 for (int j=4; j<7; j++) {
	     laby.putWall(3, j);
	 }
	 for (int j=8; j<12; j++) {
	     laby.putWall(3, j);
	 }
	 laby.putExit(0, 1);
	// // -- Fin de la première section.
	// // Deuxième section de configuration.
	// laby.putHero(1,6);
	// // -- Fin de la deuxième section.
	// // Troisième section de configuration.
	// laby.putMonster(6, 8);
	// laby.putMonster(4, 2);
	// // -- Fin de la troisième section.
	// // Quatrième section de configuration.
	// laby.putOpenDoor(3, 7);
	// laby.putClosedDoor(7, 4);
	// // -- Fin de la quatrième section.
	// -- Labyrinthe configuré.
    }

}

/**
 *  Le labyrinthe proprement dit.
 */

class Model extends Observable {
    // Un labyrinthe a : une hauteur, une largeur, un tableau de cellules,
    // un héros et une liste de monstres.

    private final int h, w;
    private Cell[][] laby;
    private Hero hero = null;

    public Cell get(int i, int j)
    {
    	return laby[i][j];
    }
    public int  getH()
    {
    	return this.h;
    }
    public int  getW()
    {
    	return this.w;
    }

    // Construction d'un labyrinthe aux dimensions données.
    // À l'origine, il n'y a ni héros ni monstre, et toutes les cases
    // sont libres.

    public Model(int h, int w)
    {
    	this.h = h;
    	this.w = w;

    	this.laby = new Cell[this.h][this.w];

    	for(int i = 0; i < h; i++)
    	{
    		for(int j = 0; j < w; j++)
    		{
    			this.laby[i][j] = new Cell(this, i, j);
    		}
    	}
    }
    // Méthode pour les déplacements du héros.
    // Déplacement d'une case dans une direction, puis notification de la vue.
    public void heroMove(Direction dir)
    {
    	hero.move(dir);
    	setChanged();
    	notifyObservers();
    }

    // Méthodes pour la configuration du labyrinthe.
    public void putCell(int i, int j) {
    	laby[i][j] = new Cell(this, i, j);
    }
    public void putWall(int i, int j)
    {
    	laby[i][j] = new Wall(this, i, j);
    }
    public void putExit(int i, int j)
    {
     	laby[i][j] = new Exit(this, i, j);
    }
    // public void putHero(int i, int j) {
    // 	if (this.hero == null) {
    // 	    hero = new Hero(laby[i][j]);
    // 	}
    // }
    // public void putMonster(int i, int j) {
    // 	monsters.add(new Monster(laby[i][j]));
    // }
    // public void putOpenDoor(int i, int j) {
    // 	laby[i][j] = Door.openDoorFactory(this, i, j);
    // }
    // public void putClosedDoor(int i, int j) {
    // 	laby[i][j] = Door.closedDoorFactory(this, i, j);
    // }
    // -- Fin des méthodes de configuration.

}

/**
 * La vue principale du labyrinthe, qui affiche l'ensemble de la structure
 * et ses occupants.
 */

class View extends JComponent implements Observer
{
    // La vue mémorise une référence au labyrinthe et à la fenêtre graphique.
    // On enregistre aussi la dimension et le côté de chaque case en pixels.
    private static final int SCALE = 40;
    private Model laby;
    private JFrame frame;
    private Dimension dim;

    // Constructeur, où la vue s'enregistre comme un élément de la fenêtre
    // graphique et comme un observateur des modifications du labyrinthe.
    public View(Model laby, JFrame frame)
    {
    	this.laby = laby;
    	this.frame = frame;
    	this.dim = new Dimension(SCALE*laby.getW(),SCALE*laby.getH());
    	this.setPreferredSize(dim);
    	laby.addObserver(this);
    	frame.add(this);
    }

    // Méthode de mise à jour pour réagir aux modification du modèle.
    public void update(Observable o, Object arg)
    {
    	repaint();
    }

    // Méthode d'affichage du labyrinthe.
    public void paintComponent(Graphics g)
    {
    	Graphics2D g2 = (Graphics2D)g;

    	for(int i = 0; i < this.laby.getH(); i++)
    	{
    		for(int j = 0; j < this.laby.getW(); j++)
    		{
    			this.laby.get(i,j).paintCell(g2, i*this.SCALE, j*this.SCALE, SCALE);
    		}
    	}
    }
}

/**
 * Le contrôleur des entrées du clavier. Il réagit aussi à la souris pour
 * récupérer le focus.
 */

class Controller extends JComponent implements KeyListener, MouseListener {
    // Le contrôleur garde une référence au labyrinthe.
    private Model laby;

    // Constructeur : le contrôleur s'enregistre comme un récepteur des entrées
    // clavier et souris, et comme un élément graphique (nécessaire pour
    // récupérer le focus et les entrées).
    public Controller(Model laby, JFrame frame) {
	this.laby = laby;
	addKeyListener(this);
	addMouseListener(this);
	setFocusable(true);
	frame.add(this);
    }

    // Méthode qui récupère l'entrée clavier et appelle l'action correspondante
    // du héros. Si l'action du héros est valide, alors les monstres sont aussi
    // déplacés.
    public void keyTyped(KeyEvent e) {
	// À compléter.
    }
    // -- Fin de l'action du clavier.

    // Récupère le focus quand on clique dans la fenêtre graphique.
    public void mouseClicked(MouseEvent e) {
	requestFocusInWindow();
    }
    // Pas de réaction aux autres stimuli.
    public void keyPressed(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
}

/**
 * À partir d'ici : les classes auxiliaires. Thèmes couverts dans l'ordre :
 * Les directions.
 * Les cases.
 * Les héros.
 */

/**
 * Directions cardinales, et équivalents en différences de coordonnées.
 */

enum Direction {
    NORTH (-1, 0),
    SOUTH ( 1, 0),
    EAST  ( 0, 1),
    WEST  ( 0,-1);

    private final int dI, dJ;
    private Direction(int di, int dj) { this.dI = di; this.dJ = dj; }
    public int dI() { return dI; }
    public int dJ() { return dJ; }
    public Direction random() { return Direction.WEST; }
}

/**
 * Cases principales du labyrinthe.
 */

class Cell {
    // On maintient une référence vers le labyrinthe et les coordonnées.
	private final Color couleur;
    private final Model laby;
    private final int i, j;

    // Constructeur.
    public Cell(Model laby, int i, int j)
    {
    	this.laby = laby;
    	this.i = i;
    	this.j = j;
    	this.couleur = Color.WHITE;
    }

    public Cell(Model laby, int i, int j, Color couleur)
    {
    	this.laby = laby;
    	this.i = i;
    	this.j = j;
    	this.couleur = couleur;
    }

    // Partie dessin.
    public void paintCell(Graphics2D g2d, int leftX, int topY, int scale)
    {
    	Rectangle2D rect = new Rectangle2D.Double(leftX, topY, scale, scale);
    	g2d.setPaint(this.couleur);
    	g2d.fill(rect);
    }
}

class Wall extends Cell
{
	Wall(Model laby, int i, int j)
	{
		super(laby, i, j, Color.BLACK);
	}
}

class Exit extends Cell
{
	Exit(Model laby, int i, int j)
	{
		super(laby, i, j, Color.BLUE);
	}
}

/**
 * La classe du héros.
 */

// Cette classe est à remanier, ce petit morceau est là juste pour éviter
// une erreur de compilation due à l'absence de la méthode [move].
class Hero {
    public void move(Direction dir) {
	// À compléter.
    }
}

