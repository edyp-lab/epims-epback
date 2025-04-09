/*
 * Created on Sep 21, 2004
 *
 * $Id: Resources.java,v 1.1 2007-09-14 09:37:29 dupierris Exp $
 */
package cea.edyp.epims.transfer.gui;


import java.awt.Color;
import java.util.ListResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

/**
 * @author BRULEY
 *
 */
public class Resources extends ListResourceBundle {
  private static String IMAGE_PATH="/cea/edyp/epims/transfer/gui/images/"; 
   
	private static final Object[][] _content = {     
      {"epback.properties", "eP-Back"},
      {"webservices.error", "Impossible d'accéder aux Web Services d'ePims"},
      {"error.panel.title", "eP-Back Error"},
      {"app.title", "eP-Back"},
      {"app.getConf.title", "eP-Back - Configuration"},
      {"getConf.instrument.title", "Instruments"},
      {"getConf.instrument.label", "Choix de l'instrument"},
      {"getConf.validate.button", "Valider"},
      {"getConf.instrument.error.msg", "Fichier de configuration des instruments introuvable."},
      {"getConf.instrument.error.title", "Configuration Instrument"},
      {"app.configuration", "Configuration"},
      {"app.analysis", "Analyses"},
      {"app.log", "Rapport"},
      {"app.start.width", 800},
      {"app.start.height", 600},
      {"exit.dialog.text", "Souhaitez-vous réellement quitter l'application BackPims ? "},
      {"exit.backup.running.dialog.text", "Attention, une sauvegarde est actuellement en cours. Souhaitez-vous réellement quitter l'application BackPims ? "},
      {"log.start", "-- Début utilisation du fichier de log {0} --"},
      {"log.end", "-- Fin utilisation du fichier de log  {0} --"},
      {"unknown.error.message","Raison inconnue"},
      
      {"epims.getinfo.error", "Un problème est survenu lors de l'accès aux informations relatives au système ePims : {0}"},
      {"remove.files.checkbox.text", "Supprimer les fichiers après copie" },
      {"operation.type.label", "Type d'opération"},
      {"operation.type.options.labels",getOperationsTypeLabelList()},
      
      //Configuration panel
      {"configuration.title", " {0} ({1}) "},   
      {"source.path.label", "répertoire source"},
      {"action.browse.label", "..."},
      {"dest.path.label", "répertoire destination"},
      {"instrument.label", "instrument:"},
      {"dataformat.no.config.msg", "Pas de configuration proposée pour ce type d'instrument."},
      {"dataformat.panel.title", "Configuration spécifique à l'instrument"},
      {"pkl.spectra.handle.checkbox.label", "Transfert des spectres (pkl, prp)"},
      {"mgf.spectra.handle.checkbox.label", "Transfert des spectres (mgf)"},
      {"pkl.path.textfield.label", "Répertoire contenant les fichiers pkl :"},
      {"pkl.path.invalid.message", "chemin invalide"},
      
      //Analysis Table 
      {"analysis.table.study.label", "Etude"},
      {"analysis.table.sample.label", "Echantillon"},
      {"analysis.table.destination.label", "Destination"},
      {"analysis.table.label", "Analyse"},
      {"analysis.table.date.label", "Date"},
      {"analysis.table.status.label","Etat"},
      {"analysis.table.size.label","Taille (Mo)"},
      {"analysis.table.duration.label","Durée (min)"},
      {"analysis.status.NOK.foreground",Color.red},
      {"analysis.status.OK.foreground",UIManager.getLookAndFeelDefaults().getColor("Label.foreground")},
      {"analysis.unselectable.foreground", Color.GRAY},
      {"analysis.selectable.foreground", Color.BLACK},
 
      //Analysis action
      {"files.refresh.label", "refresh"},
      {"files.refresh.tooltip", "Rechercher les nouvelles analyses sur le disque"},
      {"files.refresh.icon", getGUIIcon("refresh-folder")},
      {"files.epims.refresh.tooltip", "Mettre à jour le statut ePims des analyses"},
      {"files.epims.refresh.icon", getGUIIcon("refresh-analysis-status")},
      {"files.selectall.label", "tous"},
      {"files.selectall.tooltip", "Sélectionner toutes les analyses valides"},
      {"files.selectall.icon", getGUIIcon("check-all")},
      {"files.selectnone.label", "aucun"},
      {"files.selectnone.tooltip", "Désélectionner toutes les analyses"},
      {"files.selectnone.icon", getGUIIcon("uncheck-all")},
      {"files.invertselection.label", "inverser"},
      {"files.invertselection.tooltip", "Inverser la sélection des analyses"},
      {"files.invertselection.icon", getGUIIcon("inverse-check")},
      {"files.viewanalysis.icon", getGUIIcon("shown-analysis")},
      {"files.viewanalysis.tooltip", "Afficher/Cacher les analyses à sauvegarder"},
      {"files.viewsavedanalysis.icon", getGUIIcon("shown-saved-analysis")},      
      {"files.viewsavedanalysis.tooltip", "Afficher/Cacher les analyses déjà sauvegardées"},
      {"files.hideanalysis.icon", getGUIIcon("hidden-analysis")},
      {"files.hidesavedanalysis.icon", getGUIIcon("hidden-saved-analysis")},
      {"file.invalid.dest.path", "Chemin invalide"},
      {"file.invalid", "Fichier Invalid !"},
      
      {"table.list.save.label", "sauvegarder la liste"},
      
      {"status.study.close", "Etude Close"},
      {"status.study.acq.duplicate", "Acq. dupliquée"},
      {"status.study.invalid.study","Etude invalide"},
      {"status.study.invalid.sample","Ech. indéfini"},
      {"status.study.invalid.study_sample","Ech. <> Etude"},
      {"status.study.acq.exist", "sauvegardée"},
      {"status.study.ok", "OK"},
      {"status.study.unknown", "Unknown ..."},
      {"status.study.unreachable", "Inaccessible"},
      
      // Action panel
      {"action.operation.start.label", "démarrer"},
      {"action.operation.interrupt.label", "interruption en cours"},
      {"action.operation.stop.label", "interrompre"},

      {"process.error.no.selection", "Aucune analyse n'est sélectionnée"},
      {"process.error.stop.after", "!! Interruption après l'analyse "},
      {"process.error.stop.during", "!! Interruption lors du traitement de l'analyse "},
      {"process.end", "Fin du traitement des "},
      {"process.end.short", "Traitement terminé"},
      {"process.error", "Erreur lors du traitement des analyses"},
      {"process.stop", "interruption du traitement en cours"},
      {"process.stop.short", "Interruption"},
      
      {"analysis.total.nbr", "{0} analyse(s) détectée(s)"},
      {"analysis.selected.nbr", "{0} analyse(s) sélectionnée(s)"},
      {"clean.error", " - Erreur lors de la suppression de {0}"},
      {"clean.success", " - {0} supprimée ({1})"},
      {"copy.error", " - Erreur lors de la copie de {0}"},
      {"copy.success", " - {0} transféré dans {1} ({2})"},
      {"delete.error", "  !! Impossible de supprimer l'analyse {0}"}, 
      
      {"log.print.icon", getGUIIcon("print")},
      {"log.print.tooltip", "Imprimer le rapport"},
      
      
      {"raw.no.idx.file", "No index file found for {0}"},
      {"sample.invalid", "Echantillon non spécifié ou invalid"},
      
      //Operation error messages
      {"acq.creation.error", "Une erreur est survenue lors de la sauvegarde de l acquisition {0} dans ePIMS : {1}"},
      {"study.dir.notexist", "Le dossier de l'étude ou les transferts doivent s'effectuer n'existe pas : {0}"},
      {"analysis.invalid.associated.file.type", "Le type de fichier associé à l'analyse {0} est invalide"},
      {"analysis.invalid.sample.description", "Impossible d'obtenir des informations sur l'échantillon associé à l'analyse {0}"},
      {"analysis.associated.file.exist", "Le fichier associé, {0}, à l analyse {1} existe déjà"},
      {"cant.create.associated.file.directory", "Impossible de créer le dossier des fichiers associé dans le répertoire de l'étude : {0}"},
      {"analysis.path.error", "Impossible d'obtenir l information  de destination pour l'analyse {0}"},
      {"acq.more.than.one.exist", "Problème d intégrité du système ePims : plusieurs acquisitions -même nom même instrument- sont définies pour {0}"},
      {"acq.dir.notexist", "Il y a un problème avec le dossier source contenant les acquisitions (ou leurs descriptions) : {0}. Soit il n'existe pas soit il y a un problème d'accès."},
     
      //Analysis Creation Messages
      {"analysis.creation.info","Création des analyses pour le fichier {0}."},
      {"analysis.creation.error","Impossible de créer les analysis pour {0} : {1}."},
      {"analysis.set.dataformat.error","Impossible de spécifier le dataformat pour analyse {0} "},
      {"datafile.not.found","Ce fichier de donnée ne peut être trouvé : {0}"},
      
      //zip file state
      {"datafile.state.ok","Ok"},
      {"datafile.state.error","Erreur"},
      
      //bruker ressources
      {"bruker.info.spectra.handle.checkbox.label", "Afficher l'AutoX_Method et le nombre de replicats"},
      {"bruker.acq.badformat", "Erreur: l'acquisition est au mauvais format"},
      {"bruker.info.spectra.handle.checkbox.label.description", "Afficher les descriptions des analyses"}
      
  };
	
  private static ImageIcon getGUIIcon(String name){
    java.net.URL imgURL = Resources.class.getResource(IMAGE_PATH+name+".png");
    if(imgURL == null)
      imgURL = Resources.class.getResource(IMAGE_PATH+name+".gif");
    if (imgURL != null) {
      return new ImageIcon(imgURL);
    } else {
      //VD A FAIRE : Utiliser logger
      System.err.println("Couldn't find image : " + name);
      return null;
    }
  }
   
	/* (non-Javadoc)
	 * @see java.util.ListResourceBundle#getContents()
	 */
	protected Object[][] getContents() {
	  return _content;
	}
  
  protected static String[] getOperationsTypeLabelList(){
    //Index in array should follow BackupParamter transfer mode values
    String[] labels= {"Transfert vers le SAN", "Nettoyage de l'instrument"};
    return labels;
  }

}
