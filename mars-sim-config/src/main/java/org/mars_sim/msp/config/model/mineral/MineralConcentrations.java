/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.mineral;

/**
 * Class MineralConcentrations.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class MineralConcentrations implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _mineralList.
     */
    private java.util.List<org.mars_sim.msp.config.model.mineral.Mineral> _mineralList;


      //----------------/
     //- Constructors -/
    //----------------/

    public MineralConcentrations() {
        super();
        this._mineralList = new java.util.ArrayList<org.mars_sim.msp.config.model.mineral.Mineral>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vMineral
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMineral(
            final org.mars_sim.msp.config.model.mineral.Mineral vMineral)
    throws java.lang.IndexOutOfBoundsException {
        this._mineralList.add(vMineral);
    }

    /**
     * 
     * 
     * @param index
     * @param vMineral
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMineral(
            final int index,
            final org.mars_sim.msp.config.model.mineral.Mineral vMineral)
    throws java.lang.IndexOutOfBoundsException {
        this._mineralList.add(index, vMineral);
    }

    /**
     * Method enumerateMineral.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.mineral.Mineral> enumerateMineral(
    ) {
        return java.util.Collections.enumeration(this._mineralList);
    }

    /**
     * Method getMineral.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.mineral.Mineral at the given
     * index
     */
    public org.mars_sim.msp.config.model.mineral.Mineral getMineral(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._mineralList.size()) {
            throw new IndexOutOfBoundsException("getMineral: Index value '" + index + "' not in range [0.." + (this._mineralList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.mineral.Mineral) _mineralList.get(index);
    }

    /**
     * Method getMineral.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.mineral.Mineral[] getMineral(
    ) {
        org.mars_sim.msp.config.model.mineral.Mineral[] array = new org.mars_sim.msp.config.model.mineral.Mineral[0];
        return (org.mars_sim.msp.config.model.mineral.Mineral[]) this._mineralList.toArray(array);
    }

    /**
     * Method getMineralCount.
     * 
     * @return the size of this collection
     */
    public int getMineralCount(
    ) {
        return this._mineralList.size();
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    public boolean isValid(
    ) {
        try {
            validate();
        } catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * Method iterateMineral.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.mineral.Mineral> iterateMineral(
    ) {
        return this._mineralList.iterator();
    }

    /**
     * 
     * 
     * @param out
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void marshal(
            final java.io.Writer out)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws java.io.IOException if an IOException occurs during
     * marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    public void marshal(
            final org.xml.sax.ContentHandler handler)
    throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Marshaller.marshal(this, handler);
    }

    /**
     */
    public void removeAllMineral(
    ) {
        this._mineralList.clear();
    }

    /**
     * Method removeMineral.
     * 
     * @param vMineral
     * @return true if the object was removed from the collection.
     */
    public boolean removeMineral(
            final org.mars_sim.msp.config.model.mineral.Mineral vMineral) {
        boolean removed = _mineralList.remove(vMineral);
        return removed;
    }

    /**
     * Method removeMineralAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.mineral.Mineral removeMineralAt(
            final int index) {
        java.lang.Object obj = this._mineralList.remove(index);
        return (org.mars_sim.msp.config.model.mineral.Mineral) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vMineral
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setMineral(
            final int index,
            final org.mars_sim.msp.config.model.mineral.Mineral vMineral)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._mineralList.size()) {
            throw new IndexOutOfBoundsException("setMineral: Index value '" + index + "' not in range [0.." + (this._mineralList.size() - 1) + "]");
        }

        this._mineralList.set(index, vMineral);
    }

    /**
     * 
     * 
     * @param vMineralArray
     */
    public void setMineral(
            final org.mars_sim.msp.config.model.mineral.Mineral[] vMineralArray) {
        //-- copy array
        _mineralList.clear();

        for (int i = 0; i < vMineralArray.length; i++) {
                this._mineralList.add(vMineralArray[i]);
        }
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * org.mars_sim.msp.config.model.mineral.MineralConcentrations
     */
    public static org.mars_sim.msp.config.model.mineral.MineralConcentrations unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.mineral.MineralConcentrations) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.mineral.MineralConcentrations.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate(
    )
    throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
