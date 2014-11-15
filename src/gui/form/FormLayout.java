package gui.form;

import java.awt.*;
import java.util.*;

//
// (C) 1999 Steve Green
// http://www.echo-sol.com/~steveg/FormLayout
// mailto: steveg@echo-sol.com
//
// Nov 23 1999 - Fixed support for handleing insets.
// Apr 30 2001 - Fixed centering.

/** Häpeällistä: ainoa ei-oma luokka frameworkissa */
class resizeDimension extends Exception
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 3766010627687348488L;
	public resizeDimension (int w, int h) { height = h; width = w; }

    int height;
    int width;
}

class edge_constraint
{
    edge_constraint (int factor)
    {
        state = RESET;
	attachment_type = FormLayout.ATTACH_NONE;
	offset = 0;
	component = null;
	this.factor = factor;
    }

    int			location;
    int			attachment_type;
    int			offset;
    int			factor;
    form_component	component;
    char		state;

    static final char	RESET = 0;
    static final char	VISITED = 1;
    static final char	DONE = 2;
}

class form_component
{
    form_component (Component component)
    {
        this.component = component;

	constraints [FormLayout.TOP] = new edge_constraint (1);
	constraints [FormLayout.LEFT] = new edge_constraint (1);
	constraints [FormLayout.BOTTOM] = new edge_constraint (-1);
	constraints [FormLayout.RIGHT] = new edge_constraint (-1);
    }

    void get_initial_bounds (Dimension minimum_size)
    {
	Rectangle bounds = component.getBounds ();

	if (bounds.width < minimum_size.width)
	    bounds.width = minimum_size.width;
	if (bounds.height < minimum_size.height)
	    bounds.height = minimum_size.height;

	constraints [FormLayout.TOP].location = bounds.y;
	constraints [FormLayout.LEFT].location = bounds.x;
	constraints [FormLayout.BOTTOM].location = bounds.y + bounds.height - 1;
	constraints [FormLayout.RIGHT].location = bounds.x + bounds.width - 1;
    }

    void set_final_bounds ()
    {
	int x = constraints [FormLayout.LEFT].location;
	int y = constraints [FormLayout.TOP].location;
	int w = constraints [FormLayout.RIGHT].location - x + 1;
	int h = constraints [FormLayout.BOTTOM].location - y + 1;

	component.setBounds (x, y, w, h);
    }

    void move_edge (int edge, int where, boolean recompute)
	    throws resizeDimension
    {
	int diff = where - constraints [edge].location;

	int opposite_edge = edge ^ 2;

	if (should_move_too (opposite_edge))
	    constraints [opposite_edge].location += diff;
	else
	{
	    // Special Case:  If we are in "recompute" mode and we shrink
	    // because of this constraint, then we need to expand the form
	    // to accomodate us instead of shrinking the component.

	    if (recompute)
	    {
		int delta = diff * constraints [opposite_edge].factor;

		if (delta < 0)
		{
		    if (edge == FormLayout.TOP || edge == FormLayout.BOTTOM)
			throw (new resizeDimension (0, -delta));
		    else
			throw (new resizeDimension (-delta, 0));
		}
	    }
	}

	constraints [edge].location += diff;
    }

    boolean should_move_too (int edge)
    {
	// Should edge move if the opposite edge moves.  e.g. If the LEFT
	// edge moves 10 pixels, should the RIGHT edge move too?

	// Simply stated, an edge can move if there are no constraints for
	// that edge or the constraints for that edge are relative to itself
	// (specifies a width) or the edge has not been layed out yet.

	int attachment_type = constraints [edge].attachment_type;

	return (attachment_type == FormLayout.ATTACH_NONE) ||
	       (constraints [edge].state != edge_constraint.DONE) ||
	       (attachment_type == FormLayout.ATTACH_COMPONENT && 
		constraints [edge].component == this);
    }

    Component		component;
    edge_constraint	constraints [] = new edge_constraint [4];
}

public class FormLayout implements LayoutManager
{
    //////////
    ////////// Constants
    //////////

    public static final int	ATTACH_NONE = 0;
    public static final int	ATTACH_FORM = 1;
    public static final int	ATTACH_COMPONENT = 2;
    public static final int	ATTACH_OPPOSITE_COMPONENT = 3;
    public static final int	ATTACH_CENTER = 4;

    // N O T E:  These numbers ARE NOT ARBITRARY!!
    public static final int	TOP = 0;
    public static final int	LEFT = 1;
    public static final int	BOTTOM = 2;
    public static final int	RIGHT = 3;

    //////////
    ////////// Ctor
    //////////

    public FormLayout () { /* foo */ }

    public FormLayout (Container container)
    {
	container.setLayout (this);
    }

    //////////
    ////////// LayoutManager Methods
    //////////

    public void addLayoutComponent (String name, Component comp) { /* foo */ }

    public void removeLayoutComponent (Component comp)
    {
        for (int i = 0; i < component_list.size (); i++)
	{
	    form_component fc = (form_component) component_list.elementAt (i);
	    if (fc.component == comp)
	    {
	    	component_list.removeElementAt (i);
		return;
	    }
	}
    }

    public Dimension preferredLayoutSize (Container parent)
    {
	for (int i = 0; i < component_list.size (); i++)
	{
            form_component fc = (form_component) component_list.elementAt (i);
            fc.get_initial_bounds (fc.component.getPreferredSize ());
        }

	Dimension d = do_layout (parent, true);

	return d;
    }

    public Dimension minimumLayoutSize (Container parent)
    {
	for (int i = 0; i < component_list.size (); i++)
	{
            form_component fc = (form_component) component_list.elementAt (i);
            fc.get_initial_bounds (fc.component.getMinimumSize ());
        }

	Dimension d = do_layout (parent, true);

	return d;
    }

    public void layoutContainer (Container parent)
    {
	for (int i = 0; i < component_list.size (); i++)
	{
            form_component fc = (form_component) component_list.elementAt (i);
            fc.get_initial_bounds (fc.component.getPreferredSize ());
        }

	do_layout (parent, false);
    }

    public void constrain (Component comp, int edge, int attachment,
			    Component target, int offset)
    {
	//
	// Find the constraint record for this component
	//

	form_component fc = find_or_add_component (comp);
	
	if (fc == null)
	    return;

	edge_constraint ec = fc.constraints [edge];

	ec.attachment_type = attachment;
	ec.offset = offset;
	ec.component = target == null ? null : find_or_add_component (target);
    }

    //////////
    ////////// Private
    //////////

    private form_component find_component (Component comp)
    {
        for (int i = 0; i < component_list.size (); i++)
	{
	    form_component fc = (form_component) component_list.elementAt (i);
	    if (fc.component == comp)
	    	return fc;
	}

	return null;
    }

    private form_component find_or_add_component (Component comp)
    {
	form_component fc = find_component (comp);

	if (fc == null)
	{
	    fc = new form_component (comp);
	    component_list.addElement (fc);
	}

	return fc;
    }

    private Dimension do_layout (Container parent, boolean recompute)
    {
        Insets insets = parent.getInsets ();

	int my_bounds [] = new int [4];

	my_bounds [LEFT] = insets.left;
	my_bounds [TOP] = insets.top;

	if (recompute)
	{
	    my_bounds [BOTTOM] = insets.top;
	    my_bounds [RIGHT] = insets.left;
	}
	else
	{
	    Dimension d = parent.getSize ();

	    my_bounds [BOTTOM] = d.height - 1 - insets.bottom;
	    my_bounds [RIGHT] = d.width - 1 - insets.right;
	}

        for (int count = 0; count < 10000; )
	{
	    // 
	    // Reset the state of all edges.
	    //

	    for (int i = 0; i < component_list.size (); i++)
	    {
		form_component fc =
			(form_component) component_list.elementAt (i);

		for (int edge = 0; edge < 4; edge ++)
		{
		    edge_constraint ec = fc.constraints [edge];
		    ec.state = edge_constraint.RESET;
		}
	    }

	    boolean it_worked = true;

	    for (int i = 0; i < component_list.size (); i++)
	    {
		form_component fc =
			(form_component) component_list.elementAt (i);

		try
		{
		    layout (fc, my_bounds, recompute);
		}
		catch (resizeDimension d)
		{
		    if (!recompute)
		    {
			System.out.println ("This should never happen");
			return null;
		    }

		    my_bounds [BOTTOM] += d.height;
		    my_bounds [RIGHT] += d.width;

		    it_worked = false;
		    count ++;
		    break;
		}
	    }

	    if (it_worked)
	        break;
	}

	return new Dimension (my_bounds [RIGHT] + 1, my_bounds [BOTTOM] + 1);
    }

    private void layout (form_component fc, int [] my_bounds,
			    boolean recompute)
	    throws resizeDimension
    {
	for (int edge = 0; edge < 4; edge ++)
	    layout_edge (fc, edge, my_bounds, recompute);

	if (!recompute)
	    fc.set_final_bounds ();
    }

    private int layout_edge (form_component fc, int edge, int [] my_bounds,
			    boolean recompute)
	    throws resizeDimension
    {
	edge_constraint ec = fc.constraints [edge];

	if (ec.attachment_type != ATTACH_NONE &&
	    ec.state != edge_constraint.DONE)
	{
	    if (ec.state == edge_constraint.VISITED)
		System.out.println ("FormLayout.layout: Circular dependency!");
	    else
	    {
		ec.state = edge_constraint.VISITED;

		//
		// At this point, we can do the work.
		//

		int location = 0;

		switch (ec.attachment_type)
		{
		    case ATTACH_FORM:
			location = my_bounds [edge];
			break;

		    case ATTACH_COMPONENT:
			location = ec.factor +	// This IS correct
				   layout_edge (ec.component, edge ^ 2,
						my_bounds, recompute);
			break;

		    case ATTACH_OPPOSITE_COMPONENT:
			location = layout_edge (ec.component, edge,
						my_bounds, recompute);
			break;

		    case ATTACH_CENTER:
			int center;

			if (ec.component == null)	// Center on form
			{
			    center = (my_bounds [edge ^ 2] -
				      my_bounds [edge]) / 2;
			    if (center < 0) center = -center;
			}
			else
			{
			    layout_edge (ec.component, edge ^ 2,
					 my_bounds, recompute);
			    layout_edge (ec.component, edge,
					 my_bounds, recompute);
			    
			    // It might be tempting to use the return of
			    // layout_edge rather than the next 2 lines
			    // of code.  Bad idea because the second call to
			    // layout_edge may move the first edge.

			int edge1 = ec.component.constraints [edge^2].location;
			int edge2 = ec.component.constraints [edge].location;
			    int half = (edge1 - edge2) / 2;
			    center = ec.component.constraints [edge].location +
				     half;
			}

			// We depend on the opposite edge so lets lay him out
			// first.

			layout_edge (fc, edge ^ 2, my_bounds, recompute);

			int size = fc.constraints [edge ^ 2].location -
				    fc.constraints [edge].location + 1;

			location = center - size / 2;

			break;

		    default:
			System.out.println
			    ("FormLayout: Unknown attachment type!");
		}

		location += ec.offset * ec.factor;

		fc.move_edge (edge, location, recompute);

		ec.state = edge_constraint.DONE;
	    }
	}

	return fc.constraints [edge].location;
    }

    private Vector component_list = new Vector ();
}
