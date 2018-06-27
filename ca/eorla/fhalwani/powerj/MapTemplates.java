package ca.eorla.fhalwani.powerj;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import javax.swing.JOptionPane;

class MapTemplates {
	// private final byte SPECIALTY_ALL = 0;
	private final byte SPECIALTY_GENERAL = 1;
	private final byte SPECIALTY_MSK = 2;
	private final byte SPECIALTY_DERM = 3;
	private final byte SPECIALTY_LYMPHOMA = 4;
	private final byte SPECIALTY_CARDIAC = 5;
	private final byte SPECIALTY_ENT = 6;
	private final byte SPECIALTY_BREAST = 7;
	private final byte SPECIALTY_THORACIC = 8;
	private final byte SPECIALTY_GU = 9;
	private final byte SPECIALTY_GYN = 10;
	private final byte SPECIALTY_GI = 11;
	private final byte SPECIALTY_OPHTHALMIC = 12;
	// private final byte SPECIALTY_RENAL = 13;
	private final byte SPECIALTY_NEURO = 14;
	// private final byte SPECIALTY_BM = 15;
	DataMasterSpecimen thisItem = new DataMasterSpecimen();
	private HashMap<Integer, DataMasterSpecimen> masterSpecimens = new HashMap<Integer, DataMasterSpecimen>();

	MapTemplates(PowerJ parent) {
		readDB(parent);
	}
	
	void close() {
		masterSpecimens.clear();
	}
	
	DataMasterSpecimen getItem() {
		return thisItem;
	}

	byte getSubspecialtyID(String strLabel, String strDescr) {
		byte subID = thisItem.subspecialtyID;
		if (strLabel == null) {
			// Null in autopsies
			strLabel = "";
		}
		if (subID <= SPECIALTY_GENERAL
				&& (strLabel.length() > 0
				|| strDescr.length() > 0)) {
			subID = matchSpecialty(strLabel, strDescr);
		}
		return subID;
	}
	
	/** Match a specimen from PowerPath to one from PowerJ */
	boolean matchSpecimens(int templateID) {
		boolean matched = false;
		thisItem = masterSpecimens.get(templateID);
		if (thisItem != null) {
			matched = true;
		}
		return matched;
	}
	
	/** Manually match unknown sub-specialties (peritoneum, LN, FSEC, etc). */
	private byte matchSpecialty(String strLabel, String strDescr) {
		strLabel = strLabel.trim().toLowerCase();
		strDescr = strDescr.trim().toLowerCase();
		if (strLabel.equals("gi"))
			return SPECIALTY_GI;
		if (strLabel.contains("respiratory"))
			return SPECIALTY_THORACIC;
		if (strLabel.contains("mediastinum"))
			return SPECIALTY_THORACIC;
		if (strLabel.contains("extremities"))
			return SPECIALTY_MSK;
		if (strLabel.contains("placenta"))
			return SPECIALTY_GYN;
		if (strLabel.contains("salivary"))
			return SPECIALTY_ENT;
		if (strLabel.contains("thyroid"))
			return SPECIALTY_ENT;
		if (strLabel.contains("urinary"))
			return SPECIALTY_GU;
		if (strLabel.contains("breast"))
			return SPECIALTY_BREAST;
		if (strLabel.contains("kidney"))
			return SPECIALTY_GU;
		if (strLabel.contains("uterus"))
			return SPECIALTY_GYN;
		if (strLabel.contains("endocx"))
			return SPECIALTY_GYN;
		if (strLabel.contains("spleen"))
			return SPECIALTY_LYMPHOMA;
		if (strLabel.contains("marrow"))
			return SPECIALTY_LYMPHOMA;
		if (strLabel.contains("joint"))
			return SPECIALTY_MSK;
		if (strLabel.contains("heart"))
			return SPECIALTY_CARDIAC;
		if (strLabel.contains("ovary"))
			return SPECIALTY_GYN;
		if (strLabel.contains("neuro"))
			return SPECIALTY_NEURO;
		if (strLabel.contains("skin"))
			return SPECIALTY_DERM;
		if (strLabel.contains("oral"))
			return SPECIALTY_ENT;
		if (strLabel.contains("male"))
			return SPECIALTY_GU;
		if (strLabel.contains("soft"))
			return SPECIALTY_MSK;
		if (strLabel.contains("ent"))
			return SPECIALTY_ENT;
		if (strLabel.contains("liv"))
			return SPECIALTY_GI;
		if (strLabel.contains("eye"))
			return SPECIALTY_OPHTHALMIC;
		// Breast
		if (strDescr.contains("mastectomy"))
			return SPECIALTY_BREAST;
		if (strDescr.contains("breast"))
			return SPECIALTY_BREAST;
		if (strDescr.contains("nipple"))
			return SPECIALTY_BREAST;
		// Cardiac
		if (strDescr.contains("aneurysm"))
			return SPECIALTY_CARDIAC;
		if (strDescr.contains("plaque"))
			return SPECIALTY_CARDIAC;
		if (strDescr.contains("aortic"))
			return SPECIALTY_CARDIAC;
		if (strDescr.contains("aorta"))
			return SPECIALTY_CARDIAC;
		if (strDescr.contains("mitral"))
			return SPECIALTY_CARDIAC;
		if (strDescr.contains("tricuspid"))
			return SPECIALTY_CARDIAC;
		if (strDescr.contains("carotid"))
			return SPECIALTY_CARDIAC;
		if (strDescr.contains("varicose"))
			return SPECIALTY_CARDIAC;
		// Derm
		if (strDescr.contains("skin"))
			return SPECIALTY_DERM;
		if (strDescr.contains("melanoma"))
			return SPECIALTY_DERM;
		if (strDescr.contains("auricular"))
			return SPECIALTY_DERM;
		if (strDescr.contains("auricle"))
			return SPECIALTY_DERM;
		if (strDescr.contains("buttock"))
			return SPECIALTY_DERM;
		// ENT
		if (strDescr.contains("thyroid"))
			return SPECIALTY_ENT;
		if (strDescr.contains("laryngeal"))
			return SPECIALTY_ENT;
		if (strDescr.contains("larynx"))
			return SPECIALTY_ENT;
		if (strDescr.contains("pharynx"))
			return SPECIALTY_ENT;
		if (strDescr.contains("pharyngeal"))
			return SPECIALTY_ENT;
		if (strDescr.contains("glotti"))
			return SPECIALTY_ENT;
		if (strDescr.contains("tongue"))
			return SPECIALTY_ENT;
		if (strDescr.contains("tonsil"))
			return SPECIALTY_ENT;
		if (strDescr.contains("palate"))
			return SPECIALTY_ENT;
		if (strDescr.contains("submandibula"))
			return SPECIALTY_ENT;
		if (strDescr.contains("adrenal"))
			return SPECIALTY_ENT;
		if (strDescr.contains("nasal"))
			return SPECIALTY_ENT;
		if (strDescr.contains("nose"))
			return SPECIALTY_ENT;
		if (strDescr.contains("parotid"))
			return SPECIALTY_ENT;
		if (strDescr.contains("vocal"))
			return SPECIALTY_ENT;
		if (strDescr.contains("buccal"))
			return SPECIALTY_ENT;
		if (strDescr.contains("Submental"))
			return SPECIALTY_ENT;
		// GI
		if (strDescr.contains("liver"))
			return SPECIALTY_GI;
		if (strDescr.contains("hepatic"))
			return SPECIALTY_GI;
		if (strDescr.contains("pancrea"))
			return SPECIALTY_GI;
		if (strDescr.contains("rectum"))
			return SPECIALTY_GI;
		if (strDescr.contains("rectal"))
			return SPECIALTY_GI;
		if (strDescr.contains("sigmoid"))
			return SPECIALTY_GI;
		if (strDescr.contains("cecum"))
			return SPECIALTY_GI;
		if (strDescr.contains("cecal"))
			return SPECIALTY_GI;
		if (strDescr.contains("colon"))
			return SPECIALTY_GI;
		if (strDescr.contains("perianal"))
			return SPECIALTY_GI;
		if (strDescr.contains("anal"))
			return SPECIALTY_GI;
		// GU
		if (strDescr.contains("foreskin"))
			return SPECIALTY_GU;
		if (strDescr.contains("kidney"))
			return SPECIALTY_GU;
		if (strDescr.contains("renal"))
			return SPECIALTY_GU;
		if (strDescr.contains("nephric"))
			return SPECIALTY_GU;
		if (strDescr.contains("prostat"))
			return SPECIALTY_GU;
		if (strDescr.contains("ureter"))
			return SPECIALTY_GU;
		if (strDescr.contains("urethra"))
			return SPECIALTY_GU;
		if (strDescr.contains("epididym"))
			return SPECIALTY_GU;
		if (strDescr.contains("scrotum"))
			return SPECIALTY_GU;
		if (strDescr.contains("scrotal"))
			return SPECIALTY_GU;
		// Gyne
		if (strDescr.contains("vagina"))
			return SPECIALTY_GYN;
		if (strDescr.contains("vulva"))
			return SPECIALTY_GYN;
		if (strDescr.contains("uterus"))
			return SPECIALTY_GYN;
		if (strDescr.contains("uterine"))
			return SPECIALTY_GYN;
		if (strDescr.contains("ovary"))
			return SPECIALTY_GYN;
		if (strDescr.contains("ovarian"))
			return SPECIALTY_GYN;
		if (strDescr.contains("endocervi"))
			return SPECIALTY_GYN;
		if (strDescr.contains("endometri"))
			return SPECIALTY_GYN;
		if (strDescr.contains("fetus"))
			return SPECIALTY_GYN;
		if (strDescr.contains("fetal"))
			return SPECIALTY_GYN;
		// Heme - Lymphoma
		if (strDescr.contains("marrow"))
			return SPECIALTY_LYMPHOMA;
		if (strDescr.contains("iliac crest"))
			return SPECIALTY_LYMPHOMA;
		if (strDescr.contains("spleen"))
			return SPECIALTY_LYMPHOMA;
		if (strDescr.contains("splenic"))
			return SPECIALTY_LYMPHOMA;
		if (strDescr.contains("lymphoma"))
			return SPECIALTY_LYMPHOMA;
		if (strDescr.contains("leukemia"))
			return SPECIALTY_LYMPHOMA;
		// MSK
		if (strDescr.contains("sarcoma"))
			return SPECIALTY_MSK;
		if (strDescr.contains("lipoma"))
			return SPECIALTY_MSK;
		if (strDescr.contains("amputation"))
			return SPECIALTY_MSK;
		if (strDescr.contains("finger"))
			return SPECIALTY_MSK;
		if (strDescr.contains("toe"))
			return SPECIALTY_MSK;
		if (strDescr.contains("digit"))
			return SPECIALTY_MSK;
		if (strDescr.contains("subcutaneous"))
			return SPECIALTY_MSK;
		if (strDescr.contains("pannus"))
			return SPECIALTY_MSK;
		if (strDescr.contains("knee"))
			return SPECIALTY_MSK;
		if (strDescr.contains("soft tissue"))
			return SPECIALTY_MSK;
		if (strDescr.contains("hip"))
			return SPECIALTY_MSK;
		if (strDescr.contains("vertebra"))
			return SPECIALTY_MSK;
		if (strDescr.contains("sacral"))
			return SPECIALTY_MSK;
		if (strDescr.contains("tendon"))
			return SPECIALTY_MSK;
		if (strDescr.contains("bone"))
			return SPECIALTY_MSK;
		if (strDescr.contains("fracture"))
			return SPECIALTY_MSK;
		if (strDescr.contains("femur"))
			return SPECIALTY_MSK;
		if (strDescr.contains("femoral"))
			return SPECIALTY_MSK;
		if (strDescr.contains("humerus"))
			return SPECIALTY_MSK;
		if (strDescr.contains("radius"))
			return SPECIALTY_MSK;
		if (strDescr.contains("wrist"))
			return SPECIALTY_MSK;
		if (strDescr.contains("hernia"))
			return SPECIALTY_MSK;
		if (strDescr.contains("disc"))
			return SPECIALTY_MSK;
		if (strDescr.contains("lumbar"))
			return SPECIALTY_MSK;
		if (strDescr.contains("synovium"))
			return SPECIALTY_MSK;
		if (strDescr.contains("synovial"))
			return SPECIALTY_MSK;
		if (strDescr.contains("hardware"))
			return SPECIALTY_MSK;
		// Neuro
		if (strDescr.contains("brain"))
			return SPECIALTY_NEURO;
		if (strDescr.contains("frontal"))
			return SPECIALTY_NEURO;
		if (strDescr.contains("temporal"))
			return SPECIALTY_NEURO;
		if (strDescr.contains("parietal"))
			return SPECIALTY_NEURO;
		if (strDescr.contains("occipital"))
			return SPECIALTY_NEURO;
		if (strDescr.contains("cerebell"))
			return SPECIALTY_NEURO;
		if (strDescr.contains("mening"))
			return SPECIALTY_NEURO;
		if (strDescr.contains("pituitary"))
			return SPECIALTY_NEURO;
		// Ophthalmic
		if (strDescr.contains("orbit"))
			return SPECIALTY_OPHTHALMIC;
		if (strDescr.contains("cornea"))
			return SPECIALTY_OPHTHALMIC;
		if (strDescr.contains("lens"))
			return SPECIALTY_OPHTHALMIC;
		// Thoracic
		if (strDescr.contains("lung"))
			return SPECIALTY_THORACIC;
		if (strDescr.contains("pleura"))
			return SPECIALTY_THORACIC;
		if (strDescr.contains("mediastin"))
			return SPECIALTY_THORACIC;
		if (strDescr.contains("trachea"))
			return SPECIALTY_THORACIC;
		if (strDescr.contains("bronch"))
			return SPECIALTY_THORACIC;
		// Give up
		return SPECIALTY_GENERAL;
	}
	
	private void readDB(PowerJ parent) {
		ResultSet rst = parent.dbPowerJ.getMasterSpecimens(0);
		try {
			while (rst.next()) {
				thisItem = new DataMasterSpecimen();
				thisItem.specialtyID = rst.getByte("SPYID");
				thisItem.subspecialtyID = rst.getByte("SUBID");
				thisItem.procedureID = rst.getByte("PROCID");
				thisItem.hasLN = rst.getBoolean("ISLN");
				thisItem.coder1B = rst.getShort("CODE1B");
				thisItem.coder1M = rst.getShort("CODE1M");
				thisItem.coder1R = rst.getShort("CODE1R");
				thisItem.coder2B = rst.getShort("CODE2B");
				thisItem.coder2M = rst.getShort("CODE2M");
				thisItem.coder2R = rst.getShort("CODE2R");
				thisItem.coder3B = rst.getShort("CODE3B");
				thisItem.coder3M = rst.getShort("CODE3M");
				thisItem.coder3R = rst.getShort("CODE3R");
				thisItem.coder4B = rst.getShort("CODE4B");
				thisItem.coder4M = rst.getShort("CODE4M");
				thisItem.coder4R = rst.getShort("CODE4R");
				masterSpecimens.put(rst.getInt("MSID"), thisItem);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, "Templates Map", e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}
}
