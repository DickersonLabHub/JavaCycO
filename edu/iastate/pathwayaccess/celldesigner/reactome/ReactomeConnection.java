package edu.iastate.pathwayaccess.celldesigner.reactome

/**
 *  This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.ser.ArrayDeserializerFactory;
import org.apache.axis.encoding.ser.ArraySerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.encoding.ser.EnumDeserializerFactory;
import org.apache.axis.encoding.ser.EnumSerializerFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.reactome.cabig.domain.CatalystActivity;
import org.reactome.cabig.domain.Complex;
import org.reactome.cabig.domain.DatabaseCrossReference;
import org.reactome.cabig.domain.Event;
import org.reactome.cabig.domain.EventEntity;
import org.reactome.cabig.domain.EventEntitySet;
import org.reactome.cabig.domain.GeneOntology;
import org.reactome.cabig.domain.GeneOntologyRelationship;
import org.reactome.cabig.domain.GenomeEncodedEntity;
import org.reactome.cabig.domain.ModifiedResidue;
import org.reactome.cabig.domain.Pathway;
import org.reactome.cabig.domain.Polymer;
import org.reactome.cabig.domain.PublicationSource;
import org.reactome.cabig.domain.Reaction;
import org.reactome.cabig.domain.ReferenceChemical;
import org.reactome.cabig.domain.ReferenceEntity;
import org.reactome.cabig.domain.ReferenceGene;
import org.reactome.cabig.domain.ReferenceProtein;
import org.reactome.cabig.domain.ReferenceRNA;
import org.reactome.cabig.domain.ReferenceSequence;
import org.reactome.cabig.domain.Regulation;
import org.reactome.cabig.domain.RegulationType;
import org.reactome.cabig.domain.Regulator;
import org.reactome.cabig.domain.SmallMoleculeEntity;
import org.reactome.cabig.domain.Summation;
import org.reactome.cabig.domain.Taxon;
import org.reactome.servlet.InstanceNotFoundException;
import org.reactome.servlet.ReactomeRemoteException;


public class ReactomeConnection 
{
	
    private final Object[] EMPTY_ARG = new Object[]{};
    //private static Logger logger = Logger.getLogger(WSUnitTest.class);
    //private final String SERVICE_URL_NAME = "http://localhost:8080/caBIOWebApp/services/caBIOService";
    //private final String SERVICE_URL_NAME="http://stinson.cshl.edu:8080/caBIOWebApp/services/caBIOService";
    private final String SERVICE_URL_NAME="http://www.reactome.org:8080/caBIOWebApp/services/caBIOService";
    private Service caBIOService;
    Layout layout;
    WriterAppender appender;
    HashMap<String,Call> calls;
    private int MAX_RETURN;
    private HashMap<Long,Object> cache;
    private HashMap<String,Object[]> listByQueryCache;

	public ReactomeConnection() throws RemoteException 
	{
		   layout = new SimpleLayout();
		   appender = new WriterAppender();
		   appender.setLayout(layout);
		   appender.setWriter(new OutputStreamWriter(System.out));
		   appender.setThreshold(Level.ERROR);
		   BasicConfigurator.configure(appender);
		   calls = new HashMap<String,Call>();
		   addCall("listObjects");
		   addCall("listByQuery");
		   addCall("listPathwayParticipants");
		   addCall("getMaxSizeInListObjects");
		   addCall("queryByIds");
		   addCall("queryByObject");
		   addCall("queryByObjects");
		   MAX_RETURN = (Integer)calls.get("getMaxSizeInListObjects").invoke(EMPTY_ARG);
		   cache = new HashMap<Long,Object>();
		   listByQueryCache = new HashMap<String,Object[]>();
	}
	
	public Object[] queryByIds(Object[] ids) throws RemoteException
	{
		ArrayList rst = new ArrayList();
		ArrayList unknowns = new ArrayList();
		for(Object id : ids)
		{
			if(cache.containsKey(id)) rst.add(cache.get(id));
			else unknowns.add(id);
		}
		if(unknowns.size()>0)
		{
			Object[] learned = (Object[])calls.get("queryByIds").invoke(unknowns.toArray());
			for(Object o : learned)
			{
				cache.put(getId(o), o);
				//System.out.println(cache.size() +" in cache");
				rst.add(o);
			}
		}
		return rst.toArray();
		//return (Object[])calls.get("queryByIds").invoke(ids);
	}
	
	public Long getId(Object o) {
		try {
			return (Long)o.getClass().getMethod("getId", null).invoke(o, null);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("ERROR getting ID for "+o);
		}
		return null;
	}
	
	public Object load(Object o) throws RemoteException
	{
		Long id = getId(o);
		Object rst;
		if(id != null && cache.containsKey(id)) rst = cache.get(id);
		else
		{
			rst = calls.get("queryByObject").invoke(new Object[] {o});
			cache.put(id,rst);
//			try {
//				this.print(rst);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			//System.out.println(cache.size() +" in cache");
		}
		return rst;
		//return calls.get("queryByObject").invoke(new Object[] {o});
	}
	
	public Object[] load(Object[] objects) throws RemoteException
	{
		ArrayList rst = new ArrayList();
		ArrayList unknowns = new ArrayList();
		for(Object o : objects)
		{
			Long id = getId(o);
			if(cache.containsKey(id)) rst.add(cache.get(id));
			else unknowns.add(o);
		}
		if(unknowns.size()>0)
		{
			Object[] learned = (Object[])calls.get("queryByObjects").invoke(new Object[] {unknowns.toArray()});
			for(Object o : learned)
			{
				cache.put(getId(o), o);
				//System.out.println(cache.size() +" in cache");
				rst.add(o);
//				try {
//					this.print(o);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
		}
		return rst.toArray();
		//return (Object[])calls.get("queryByObjects").invoke(new Object[] {objects});
	}
	
	private void addCall(String name)
	{
		try
		{
			calls.put(name,createCall(name));
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public Object[] listObjects(Class type,int from,int n) throws RemoteException
	{
		if(n>MAX_RETURN) n=MAX_RETURN;
		return (Object[])calls.get("listObjects").invoke(new Object[] {type.getName(),from,n});
	}
	
	public Object[] listObjects(Class type) throws RemoteException
	{
		   int c = 0;
		   ArrayList rst = new ArrayList();
		   for(int i=0;true;i+=MAX_RETURN)
		   {
			   Object[] rtn = listObjects(type,i,MAX_RETURN);
			   if(rtn.length==0) break;
			   c+=rtn.length;
			   for(Object o : rtn)
			   {
				   rst.add(o);
			   }
			   if(rtn.length<MAX_RETURN) break;
		   }
		   return rst.toArray();
	}
	
	public Object[] listByQuery(Class type,String field,Object value) throws RemoteException
	{
		String key = type.getName()+field+getId(value);
		if(listByQueryCache.containsKey(key)) return listByQueryCache.get(key);
		else
		{
			Object[] rst = (Object[])calls.get("listByQuery").invoke(new Object[] {type.getName(),field,value});
			listByQueryCache.put(key, rst);
			//System.out.println(listByQueryCache.size() +" in listByQueryCache");
			return rst;
		}
	}

	private Call createCall(String callName) throws Exception {
		if (caBIOService == null) {
			QName n = new QName(SERVICE_URL_NAME, "CaBioDomainWSEndPointService");
			caBIOService = new Service(SERVICE_URL_NAME + "?wsdl", n);
		}
		String portName = "caBIOService";
		Call call = (Call) caBIOService.createCall(new QName(SERVICE_URL_NAME, portName),
				callName);
		registerTypeMappings(call);
		return call;
	}

	public static void print(Object[] objects) throws Exception
	{

		for(Object obj : objects)
		{
			print(obj);
		}
		System.out.print(objects.length);
	}

	public static void print(Object obj) throws Exception
	{
		System.out.println("------------------------\n"+obj.getClass()+" -> "+obj.toString());
		TreeSet<String> rst = new TreeSet<String>();
		for(Method m : (obj.getClass().getMethods()))
		{
			String mName = m.getName();
			if(mName.startsWith("get") && m.getGenericParameterTypes().length==0)
			{
				String propName = mName.substring(3);
				//System.out.println("\t"+propName+": "+m.invoke(obj));
				rst.add(propName+": "+m.invoke(obj));
			}
			else rst.add(mName+": ???");
		}
		for(String s : rst)
		{
			System.out.println(s);
		}

	}
	
	

	private void registerTypeMappings(Call call) {
		QName instanceNotFoundModel = new QName("http://www.reactome.org/caBIOWebApp/schema", 
		"InstanceNotFoundException");
		call.registerTypeMapping(InstanceNotFoundException.class, instanceNotFoundModel,
				new BeanSerializerFactory(InstanceNotFoundException.class, instanceNotFoundModel),
				new BeanDeserializerFactory(InstanceNotFoundException.class, instanceNotFoundModel));
		QName reactomeAxisFaultModel = new QName("http://www.reactome.org/caBIOWebApp/schema", 
		"ReactomeRemoteException");
		call.registerTypeMapping(ReactomeRemoteException.class, reactomeAxisFaultModel,
				new BeanSerializerFactory(ReactomeRemoteException.class, reactomeAxisFaultModel),
				new BeanDeserializerFactory(ReactomeRemoteException.class, reactomeAxisFaultModel));
		QName CatalystActivityModel= new QName("http://www.reactome.org/caBIOWebApp/schema", 
		"CatalystActivity");
		call.registerTypeMapping(CatalystActivity.class, CatalystActivityModel,
				new BeanSerializerFactory(CatalystActivity.class, CatalystActivityModel),
				new BeanDeserializerFactory(CatalystActivity.class, CatalystActivityModel));
		QName ComplexModel= new QName("http://www.reactome.org/caBIOWebApp/schema", 
		"Complex");
		call.registerTypeMapping(Complex.class, ComplexModel,
				new BeanSerializerFactory(Complex.class, ComplexModel),
				new BeanDeserializerFactory(Complex.class, ComplexModel));
		QName DatabaseCrossReferenceModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "DatabaseCrossReference");
		call.registerTypeMapping(DatabaseCrossReference.class, DatabaseCrossReferenceModel,
				new BeanSerializerFactory(DatabaseCrossReference.class, DatabaseCrossReferenceModel),
				new BeanDeserializerFactory(DatabaseCrossReference.class, DatabaseCrossReferenceModel));
		QName EventModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "Event");
		call.registerTypeMapping(Event.class, EventModel,
				new BeanSerializerFactory(Event.class, EventModel),
				new BeanDeserializerFactory(Event.class, EventModel));
		QName EventEntityModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "EventEntity");
		call.registerTypeMapping(EventEntity.class, EventEntityModel,
				new BeanSerializerFactory(EventEntity.class, EventEntityModel),
				new BeanDeserializerFactory(EventEntity.class, EventEntityModel));
		QName EventEntitySetModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "EventEntitySet");
		call.registerTypeMapping(EventEntitySet.class, EventEntitySetModel,
				new BeanSerializerFactory(EventEntitySet.class, EventEntitySetModel),
				new BeanDeserializerFactory(EventEntitySet.class, EventEntitySetModel));
		QName GeneOntologyModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "GeneOntology");
		call.registerTypeMapping(GeneOntology.class, GeneOntologyModel,
				new BeanSerializerFactory(GeneOntology.class, GeneOntologyModel),
				new BeanDeserializerFactory(GeneOntology.class, GeneOntologyModel));
		QName GeneOntologyRelationshipModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "GeneOntologyRelationship");
		call.registerTypeMapping(GeneOntologyRelationship.class, GeneOntologyRelationshipModel,
				new BeanSerializerFactory(GeneOntologyRelationship.class, GeneOntologyRelationshipModel),
				new BeanDeserializerFactory(GeneOntologyRelationship.class, GeneOntologyRelationshipModel));
		QName GenomeEncodedEntityModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "GenomeEncodedEntity");
		call.registerTypeMapping(GenomeEncodedEntity.class, GenomeEncodedEntityModel,
				new BeanSerializerFactory(GenomeEncodedEntity.class, GenomeEncodedEntityModel),
				new BeanDeserializerFactory(GenomeEncodedEntity.class, GenomeEncodedEntityModel));
		QName ModifiedResidueModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "ModifiedResidue");
		call.registerTypeMapping(ModifiedResidue.class, ModifiedResidueModel,
				new BeanSerializerFactory(ModifiedResidue.class, ModifiedResidueModel),
				new BeanDeserializerFactory(ModifiedResidue.class, ModifiedResidueModel));
		QName PathwayModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "Pathway");
		call.registerTypeMapping(Pathway.class, PathwayModel,
				new BeanSerializerFactory(Pathway.class, PathwayModel),
				new BeanDeserializerFactory(Pathway.class, PathwayModel));
		QName PolymerModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "Polymer");
		call.registerTypeMapping(Polymer.class, PolymerModel,
				new BeanSerializerFactory(Polymer.class, PolymerModel),
				new BeanDeserializerFactory(Polymer.class, PolymerModel));
		QName PublicationSourceModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "PublicationSource");
		call.registerTypeMapping(PublicationSource.class, PublicationSourceModel,
				new BeanSerializerFactory(PublicationSource.class, PublicationSourceModel),
				new BeanDeserializerFactory(PublicationSource.class, PublicationSourceModel));
		QName ReactionModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "Reaction");
		call.registerTypeMapping(Reaction.class, ReactionModel,
				new BeanSerializerFactory(Reaction.class, ReactionModel),
				new BeanDeserializerFactory(Reaction.class, ReactionModel));
		QName ReferenceChemicalModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "ReferenceChemical");
		call.registerTypeMapping(ReferenceChemical.class, ReferenceChemicalModel,
				new BeanSerializerFactory(ReferenceChemical.class, ReferenceChemicalModel),
				new BeanDeserializerFactory(ReferenceChemical.class, ReferenceChemicalModel));
		QName ReferenceEntityModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "ReferenceEntity");
		call.registerTypeMapping(ReferenceEntity.class, ReferenceEntityModel,
				new BeanSerializerFactory(ReferenceEntity.class, ReferenceEntityModel),
				new BeanDeserializerFactory(ReferenceEntity.class, ReferenceEntityModel));
		QName ReferenceGeneModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "ReferenceGene");
		call.registerTypeMapping(ReferenceGene.class, ReferenceGeneModel,
				new BeanSerializerFactory(ReferenceGene.class, ReferenceGeneModel),
				new BeanDeserializerFactory(ReferenceGene.class, ReferenceGeneModel));
		QName ReferenceProteinModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "ReferenceProtein");
		call.registerTypeMapping(ReferenceProtein.class, ReferenceProteinModel,
				new BeanSerializerFactory(ReferenceProtein.class, ReferenceProteinModel),
				new BeanDeserializerFactory(ReferenceProtein.class, ReferenceProteinModel));
		QName ReferenceRNAModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "ReferenceRNA");
		call.registerTypeMapping(ReferenceRNA.class, ReferenceRNAModel,
				new BeanSerializerFactory(ReferenceRNA.class, ReferenceRNAModel),
				new BeanDeserializerFactory(ReferenceRNA.class, ReferenceRNAModel));
		QName ReferenceSequenceModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "ReferenceSequence");
		call.registerTypeMapping(ReferenceSequence.class, ReferenceSequenceModel,
				new BeanSerializerFactory(ReferenceSequence.class, ReferenceSequenceModel),
				new BeanDeserializerFactory(ReferenceSequence.class, ReferenceSequenceModel));
		QName RegulationModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "Regulation");
		call.registerTypeMapping(Regulation.class, RegulationModel,
				new BeanSerializerFactory(Regulation.class, RegulationModel),
				new BeanDeserializerFactory(Regulation.class, RegulationModel));
		QName RegulationTypeModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "RegulationType");
		call.registerTypeMapping(RegulationType.class, RegulationTypeModel,
				new EnumSerializerFactory(RegulationType.class, RegulationTypeModel),
				new EnumDeserializerFactory(RegulationType.class, RegulationTypeModel));
		QName RegulatorModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "Regulator");
		call.registerTypeMapping(Regulator.class, RegulatorModel,
				new BeanSerializerFactory(Regulator.class, RegulatorModel),
				new BeanDeserializerFactory(Regulator.class, RegulatorModel));
		QName SmallMoleculeEntityModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "SmallMoleculeEntity");
		call.registerTypeMapping(SmallMoleculeEntity.class, SmallMoleculeEntityModel,
				new BeanSerializerFactory(SmallMoleculeEntity.class, SmallMoleculeEntityModel),
				new BeanDeserializerFactory(SmallMoleculeEntity.class, SmallMoleculeEntityModel));
		QName SummationModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "Summation");
		call.registerTypeMapping(Summation.class, SummationModel,
				new BeanSerializerFactory(Summation.class, SummationModel),
				new BeanDeserializerFactory(Summation.class, SummationModel));
		QName TaxonModel= new QName("http://www.reactome.org/caBIOWebApp/schema", "Taxon");
		call.registerTypeMapping(Taxon.class, TaxonModel,
				new BeanSerializerFactory(Taxon.class, TaxonModel),
				new BeanDeserializerFactory(Taxon.class, TaxonModel));
		// {http://localhost:8080/caBIOWebApp/services/caBIOService}ArrayOf_xsd_anyType
		QName arrayModel = new QName("http://www.reactome.org/caBIOWebApp/services/caBIOService", "ArrayOf_xsd_anyType");
		QName componentModel = new QName("http://www.w3.org/2001/XMLSchema", "anyType");
		call.registerTypeMapping(Object[].class, arrayModel,
				new ArraySerializerFactory(Object.class, componentModel),
				new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://localhost:8080/caBIOWebApp/services/caBIOService", "queryByIds");
		//componentModel = new QName("http://www.w3.org/2001/XMLSchema", "long");
		//call.registerTypeMapping(Long[].class, arrayModel,
		//       new ArraySerializerFactory(Long.class, componentModel),
		//       new ArrayDeserializerFactory(componentModel));
		arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfAnyType");
		componentModel = new QName("http://www.w3.org/2001/XMLSchema", "anyType");
		call.registerTypeMapping(Object.class, arrayModel,
				new ArraySerializerFactory(Object.class, componentModel),
				new ArrayDeserializerFactory(componentModel));

		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfPathway");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "Pathway");
		//call.registerTypeMapping(Pathway[].class, arrayModel,
		//       new ArraySerializerFactory(ReferenceEntity.class, componentModel),
		//       new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfEventEntity");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "EventEntity");
		//call.registerTypeMapping(EventEntity[].class, arrayModel,
		//       new ArraySerializerFactory(EventEntity.class, componentModel),
		//       new ArrayDeserializerFactory(componentModel));
		//
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfCatalystActivity");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "CatalystActivity");
		//call.registerTypeMapping(CatalystActivity[].class, arrayModel,
		//   new ArraySerializerFactory(CatalystActivity.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfComplex");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "Complex");
		//call.registerTypeMapping(Complex[].class, arrayModel,
		//   new ArraySerializerFactory(Complex.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfDatabaseCrossReference");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "DatabaseCrossReference");
		//call.registerTypeMapping(DatabaseCrossReference[].class, arrayModel,
		//   new ArraySerializerFactory(DatabaseCrossReference.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfEvent");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "Event");
		//call.registerTypeMapping(Event[].class, arrayModel,
		//   new ArraySerializerFactory(Event.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfEventEntity");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "EventEntity");
		//call.registerTypeMapping(EventEntity[].class, arrayModel,
		//   new ArraySerializerFactory(EventEntity.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfEventEntitySet");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "EventEntitySet");
		//call.registerTypeMapping(EventEntitySet[].class, arrayModel,
		//   new ArraySerializerFactory(EventEntitySet.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfEventSet");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "EventSet");
		//call.registerTypeMapping(EventSet[].class, arrayModel,
		//   new ArraySerializerFactory(EventSet.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfGeneOntology");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "GeneOntology");
		//call.registerTypeMapping(GeneOntology[].class, arrayModel,
		//   new ArraySerializerFactory(GeneOntology.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfGeneOntologyRelationship");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "GeneOntologyRelationship");
		//call.registerTypeMapping(GeneOntologyRelationship[].class, arrayModel,
		//   new ArraySerializerFactory(GeneOntologyRelationship.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfGenomeEncodedEntity");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "GenomeEncodedEntity");
		//call.registerTypeMapping(GenomeEncodedEntity[].class, arrayModel,
		//   new ArraySerializerFactory(GenomeEncodedEntity.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfModifiedResidue");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ModifiedResidue");
		//call.registerTypeMapping(ModifiedResidue[].class, arrayModel,
		//   new ArraySerializerFactory(ModifiedResidue.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfPathway");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "Pathway");
		//call.registerTypeMapping(Pathway[].class, arrayModel,
		//   new ArraySerializerFactory(Pathway.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfPolymer");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "Polymer");
		//call.registerTypeMapping(Polymer[].class, arrayModel,
		//   new ArraySerializerFactory(Polymer.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfPublicationSource");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "PublicationSource");
		//call.registerTypeMapping(PublicationSource[].class, arrayModel,
		//   new ArraySerializerFactory(PublicationSource.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfReaction");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "Reaction");
		//call.registerTypeMapping(Reaction[].class, arrayModel,
		//   new ArraySerializerFactory(Reaction.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfReferenceChemical");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ReferenceChemical");
		//call.registerTypeMapping(ReferenceChemical[].class, arrayModel,
		//   new ArraySerializerFactory(ReferenceChemical.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfReferenceEntity");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ReferenceEntity");
		//call.registerTypeMapping(ReferenceEntity[].class, arrayModel,
		//   new ArraySerializerFactory(ReferenceEntity.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfReferenceGene");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ReferenceGene");
		//call.registerTypeMapping(ReferenceGene[].class, arrayModel,
		//   new ArraySerializerFactory(ReferenceGene.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfReferenceProtein");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ReferenceProtein");
		//call.registerTypeMapping(ReferenceProtein[].class, arrayModel,
		//   new ArraySerializerFactory(ReferenceProtein.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfReferenceRNA");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ReferenceRNA");
		//call.registerTypeMapping(ReferenceRNA[].class, arrayModel,
		//   new ArraySerializerFactory(ReferenceRNA.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfReferenceSequence");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ReferenceSequence");
		//call.registerTypeMapping(ReferenceSequence[].class, arrayModel,
		//   new ArraySerializerFactory(ReferenceSequence.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfRegulation");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "Regulation");
		//call.registerTypeMapping(Regulation[].class, arrayModel,
		//   new ArraySerializerFactory(Regulation.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfRegulationType");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "RegulationType");
		//call.registerTypeMapping(RegulationType[].class, arrayModel,
		//   new ArraySerializerFactory(RegulationType.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfRegulator");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "Regulator");
		//call.registerTypeMapping(Regulator[].class, arrayModel,
		//   new ArraySerializerFactory(Regulator.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfSmallMoleculeEntity");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "SmallMoleculeEntity");
		//call.registerTypeMapping(SmallMoleculeEntity[].class, arrayModel,
		//   new ArraySerializerFactory(SmallMoleculeEntity.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfSummation");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "Summation");
		//call.registerTypeMapping(Summation[].class, arrayModel,
		//   new ArraySerializerFactory(Summation.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
		//arrayModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "ArrayOfTaxon");
		//componentModel = new QName("http://www.reactome.org/caBIOWebApp/schema", "Taxon");
		//call.registerTypeMapping(Taxon[].class, arrayModel,
		//   new ArraySerializerFactory(Taxon.class, componentModel),
		//   new ArrayDeserializerFactory(componentModel));
	}

}
