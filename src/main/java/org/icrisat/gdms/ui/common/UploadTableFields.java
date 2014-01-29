package org.icrisat.gdms.ui.common;

import org.icrisat.gdms.ui.FieldProperties;
import org.icrisat.gdms.upload.marker.UploadField;

public interface UploadTableFields {

	public FieldProperties[] SSRMarker = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.MarkerName.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Alias.toString(), "", "", ""),
			new FieldProperties(UploadField.Crop.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Genotype.toString(), "", "", ""),
			new FieldProperties(UploadField.Ploidy.toString(), "", "", ""),
			new FieldProperties(UploadField.GID.toString(), "", "", ""),
			new FieldProperties(UploadField.PrincipleInvestigator.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Contact.toString(), "", "", ""),
			new FieldProperties(UploadField.Institute.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.InchargePerson.toString(), "", "", ""),
			new FieldProperties(UploadField.AssayType.toString(), "", "", ""),
			new FieldProperties(UploadField.Repeat.toString(), "", "", ""),
			new FieldProperties(UploadField.NoOfRepeats.toString(), "", "", ""),
			new FieldProperties(UploadField.SSRType.toString(), "", "", ""),
			new FieldProperties(UploadField.Sequence.toString(), "", "", ""),		
			new FieldProperties(UploadField.SequenceLength.toString(), "", "", ""),
			new FieldProperties(UploadField.MinAllele.toString(), "", "", ""),
			new FieldProperties(UploadField.MaxAllele.toString(), "", "", ""),
			new FieldProperties(UploadField.SSRNumber.toString(), "", "", ""),	
			new FieldProperties(UploadField.SizeOfRepeatMotif.toString(), "", "", ""),	
			new FieldProperties(UploadField.ForwardPrimer.toString(), UploadField.REQUIRED.toString(), "", ""),	
			new FieldProperties(UploadField.ReversePrimer.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.ProductSize.toString(), "", "", ""),
			new FieldProperties(UploadField.PrimerLength.toString(), "", "", ""),
			new FieldProperties(UploadField.ForwardPrimerTemperature.toString(), "", "", ""),
			new FieldProperties(UploadField.ReversePrimerTemperature.toString(), "", "", ""),
			new FieldProperties(UploadField.AnnealingTemperature.toString(), "", "", ""),
			new FieldProperties(UploadField.ElongationTemperature.toString(), "", "", ""),
			new FieldProperties(UploadField.FragmentSizeExpected.toString(), "", "", ""),
			new FieldProperties(UploadField.FragmentSizeObserved.toString(), "", "", ""),	
			new FieldProperties(UploadField.Amplification.toString(), "", "", ""),
			new FieldProperties(UploadField.Reference.toString(), "", "", "")};


	public FieldProperties[] SNPMarker = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.MarkerName.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Alias.toString(), "", "", ""),
			new FieldProperties(UploadField.Crop.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Genotype.toString(), "", "", ""),
			new FieldProperties(UploadField.Ploidy.toString(), "", "", ""),
			new FieldProperties(UploadField.GID.toString(), "", "", ""),
			new FieldProperties(UploadField.PrincipleInvestigator.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Contact.toString(), "", "", ""),
			new FieldProperties(UploadField.Institute.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.InchargePerson.toString(), "", "", ""),
			new FieldProperties(UploadField.AssayType.toString(), "", "", ""),	
			new FieldProperties(UploadField.ForwardPrimer.toString(), "", "", ""),	
			new FieldProperties(UploadField.ReversePrimer.toString(), "", "", ""),
			new FieldProperties(UploadField.ProductSize.toString(), "", "", ""),
			new FieldProperties(UploadField.ExpectedProductSize.toString(), "", "", ""),
			new FieldProperties(UploadField.PositionOnReferenceSequence.toString(), "", "", ""),
			new FieldProperties(UploadField.Motif.toString(), "", "", ""),
			new FieldProperties(UploadField.AnnealingTemperature.toString(), "", "", ""),
			new FieldProperties(UploadField.Sequence.toString(), "", "", ""),
			new FieldProperties(UploadField.Reference.toString(), "", "", "")};


	public FieldProperties[] CISRMarker = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.MarkerName.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.PrimerID.toString(), "", "", ""),
			new FieldProperties(UploadField.Alias.toString(), "", "", ""),
			new FieldProperties(UploadField.Crop.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Genotype.toString(), "", "", ""),
			new FieldProperties(UploadField.Ploidy.toString(), "", "", ""),
			new FieldProperties(UploadField.GID.toString(), "", "", ""),
			new FieldProperties(UploadField.PrincipleInvestigator.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Contact.toString(), "", "", ""),
			new FieldProperties(UploadField.Institute.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.InchargePerson.toString(), "", "", ""),
			new FieldProperties(UploadField.AssayType.toString(), "", "", ""),
			new FieldProperties(UploadField.Repeat.toString(), "", "", ""),
			new FieldProperties(UploadField.NoOfRepeats.toString(), "", "", ""),
			new FieldProperties(UploadField.Sequence.toString(), "", "", ""),
			new FieldProperties(UploadField.SequenceLength.toString(), "", "", ""),
			new FieldProperties(UploadField.MinAllele.toString(), "", "", ""),
			new FieldProperties(UploadField.MaxAllele.toString(), "", "", ""),
			new FieldProperties(UploadField.SizeOfRepeatMotif.toString(), "", "", ""),
			new FieldProperties(UploadField.ForwardPrimer.toString(), UploadField.REQUIRED.toString(), "", ""),	
			new FieldProperties(UploadField.ReversePrimer.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.ProductSize.toString(), "", "", ""),
			new FieldProperties(UploadField.PrimerLength.toString(), "", "", ""),
			new FieldProperties(UploadField.ForwardPrimerTemperature.toString(), "", "", ""),
			new FieldProperties(UploadField.ReversePrimerTemperature.toString(), "", "", ""),
			new FieldProperties(UploadField.AnnealingTemperature.toString(), "", "", ""),
			new FieldProperties(UploadField.FragmentSizeExpected.toString(), "", "", ""),
			new FieldProperties(UploadField.Amplification.toString(), "", "", ""),
			new FieldProperties(UploadField.Reference.toString(), "", "", ""),
			new FieldProperties(UploadField.Remarks.toString(), "", "", "")};

	public FieldProperties[] CAPMarker = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.MarkerName.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.PrimerID.toString(), "", "", ""),
			new FieldProperties(UploadField.Alias.toString(), "", "", ""),
			new FieldProperties(UploadField.Crop.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Genotype.toString(), "", "", ""),
			new FieldProperties(UploadField.Ploidy.toString(), "", "", ""),
			new FieldProperties(UploadField.GID.toString(), "", "", ""),
			new FieldProperties(UploadField.PrincipleInvestigator.toString(), "", "", ""),
			new FieldProperties(UploadField.Contact.toString(), "", "", ""),
			new FieldProperties(UploadField.Institute.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.InchargePerson.toString(), "", "", ""),
			new FieldProperties(UploadField.AssayType.toString(), "", "", ""),
			new FieldProperties(UploadField.ForwardPrimer.toString(), "", "", ""),	
			new FieldProperties(UploadField.ReversePrimer.toString(), "", "", ""),
			new FieldProperties(UploadField.ProductSize.toString(), "", "", ""),
			new FieldProperties(UploadField.ExpectedProductSize.toString(), "", "", ""),
			new FieldProperties(UploadField.RestrictionEnzymeForAssay.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.PositionOnReferenceSequence.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Motif.toString(), "", "", ""),
			new FieldProperties(UploadField.AnnealingTemperature.toString(), "", "", ""),
			new FieldProperties(UploadField.Sequence.toString(), "", "", ""),
			new FieldProperties(UploadField.Reference.toString(), "", "", ""),
			new FieldProperties(UploadField.Remarks.toString(), "", "", "")};

	public FieldProperties[] SSRGenotype_Source = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.Institute.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.PrincipleInvestigator.toString(), "", "", ""),
			new FieldProperties(UploadField.DatasetName.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.DatasetDescription.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Genus.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Species.toString(), "", "", ""),
			new FieldProperties(UploadField.MissingData.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Remark.toString(), "", "", "")};

	public FieldProperties[] SSRGenotype_Data = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.GID.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Accession.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Marker.toString(), UploadField.REQUIRED.toString(), "", ""),	
			new FieldProperties(UploadField.GelOrRun.toString(), "", "", ""),	
			new FieldProperties(UploadField.Dye.toString(), "", "", ""),	
			new FieldProperties(UploadField.CalledAllele.toString(), "", "", ""),
			new FieldProperties(UploadField.RawData.toString(), "", "", ""),	
			new FieldProperties(UploadField.Quality.toString(), "", "", ""),	
			new FieldProperties(UploadField.Height.toString(), "", "", ""),	
			new FieldProperties(UploadField.Volume.toString(), "", "", ""),	
			new FieldProperties(UploadField.Amount.toString(), UploadField.REQUIRED.toString(), "", "")};


	public FieldProperties[] SNPGenotype_Source = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.Institute.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.PI.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Email.toString(), "", "", ""),
			new FieldProperties(UploadField.InchargePerson.toString(), "", "", ""),
			new FieldProperties(UploadField.DatasetName.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.PurposeOfTheStudy.toString(), "", "", ""),
			new FieldProperties(UploadField.DatasetDescription.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Genus.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Species.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.MissingData.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.CreationDate.toString(), "", "", "")};

	public FieldProperties[] SNPGenotype_Data = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.GID.toString(), UploadField.REQUIRED.toString(), "", ""),	
			new FieldProperties(UploadField.Genotype.toString(), UploadField.REQUIRED.toString(), "", "")};
	
	//20131209: Tulasi --- Added fields required for KBio Science Genotype upload
	public FieldProperties[] LGCGenomicsSNPGenotype_Source = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.KBioSciencesGridReport.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.GridVersion.toString(), "", "", ""),
			new FieldProperties(UploadField.ProjectNumber.toString(), "", "", ""),
			new FieldProperties(UploadField.OrderNumber.toString(), "", "", ""),
			new FieldProperties(UploadField.Plates.toString(), "", "", "")};


	public FieldProperties[] LGCGenomicsSNPGenotype_Data = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.DNA.toString(), UploadField.REQUIRED.toString(), "", ""),	
			//new FieldProperties(UploadField.Assay.toString(), UploadField.REQUIRED.toString(), "", "")
			};
	//20131209: Tulasi --- Added fields required for KBio Science Genotype upload

	public FieldProperties[] DArtGenotype_Source = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.Institute.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.PrincipleInvestigator.toString(), "", "", ""),
			new FieldProperties(UploadField.DatasetName.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.DatasetDescription.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Genus.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Species.toString(), "", "", ""),
			new FieldProperties(UploadField.Remark.toString(), "", "", "")};


	public FieldProperties[] DArtGenotype_Data = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.CloneID.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.MarkerName.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Q.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Reproducibility.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.CallRate.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.PIC.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Discordance.toString(), UploadField.REQUIRED.toString(), "", "")};


	public FieldProperties[] DArtGenotype_GID = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.GIDs.toString(), UploadField.REQUIRED.toString(), "", ""),	
			new FieldProperties(UploadField.GermplasmName.toString(), UploadField.REQUIRED.toString(), "", "")};


	public FieldProperties[] MappingGenotype_Source = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.Institute.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.PrincipleInvestigator.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.EmailContact.toString(), "", "", ""),
			new FieldProperties(UploadField.DatasetName.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.DatasetDescription.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Genus.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Species.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.PopulationID.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.ParentAGID.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.ParentA.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.ParentBGID.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.ParentB.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.PopulationSize.toString(), "", "", ""),
			new FieldProperties(UploadField.PopulationType.toString(), "", "", ""),
			new FieldProperties(UploadField.PurposeOfTheStudy.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.ScoringScheme.toString(), "", "", ""),
			new FieldProperties(UploadField.MissingData.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.CreationDate.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Remark.toString(), "", "", "")};

	public FieldProperties[] MappingGenotype_Data = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.Alias.toString(), "", "", ""),	
			new FieldProperties(UploadField.GID.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Line.toString(), UploadField.REQUIRED.toString(), "", "")};

	public FieldProperties[] QTL_Source = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.Institute.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.PrincipleInvestigator.toString(), "", "", ""),
			new FieldProperties(UploadField.DatasetName.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.DatasetDescription.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Genus.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Method.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Score.toString(), UploadField.REQUIRED.toString(), "", ""),

			new FieldProperties(UploadField.Species.toString(), "", "", ""),
			new FieldProperties(UploadField.Remark.toString(), "", "", "")};		

	public FieldProperties[] QTL_Data = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.Name.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Chromosome.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.MapName.toString(), UploadField.REQUIRED.toString(), "", ""),	
			new FieldProperties(UploadField.Position.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.PosMin.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.PosMax.toString(), UploadField.REQUIRED.toString(), "", ""),
			//new FieldProperties(UploadField.Trait.toString(), UploadField.REQUIRED.toString(), "", ""),	
			new FieldProperties(UploadField.TraitID.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Experiment.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Clen.toString(), "", "", ""),		
			new FieldProperties(UploadField.LFM.toString(), UploadField.REQUIRED.toString(), "", ""),	
			new FieldProperties(UploadField.RFM.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Effect.toString(), UploadField.REQUIRED.toString(), "", ""),			
			new FieldProperties(UploadField.additive.toString(),"", "", ""),
			new FieldProperties(UploadField.HighParent.toString(),"", "", ""),
			new FieldProperties(UploadField.HighValueAllele.toString(),"", "", ""),
			new FieldProperties(UploadField.LowParent.toString(),"", "",""),
			new FieldProperties(UploadField.LowAllele.toString(),"", "",""),		
			new FieldProperties(UploadField.LOD.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.R2.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Interactions.toString(), "", "", "")};


	public FieldProperties[] Map_Source = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.MapName.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.MapDescription.toString(), "", "", ""),
			new FieldProperties(UploadField.Crop.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.MapUnit.toString(), UploadField.REQUIRED.toString(), "", ""),};


	public FieldProperties[] Map_Data = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.MarkerName.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.LinkageGroup.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Position.toString(), "", "", "")
	};

	/**
	 * ADDRED BY KALYANI 13112013
	 * 
	 */
	
	public FieldProperties[] MTA_Source = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.Institute.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.PrincipleInvestigator.toString(), "", "", ""),
			new FieldProperties(UploadField.DatasetName.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.DatasetDescription.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Genus.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Method.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Score.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Species.toString(), "", "", ""),
			new FieldProperties(UploadField.Remark.toString(), "", "", "")};		

	public FieldProperties[] MTA_Data = {
			new FieldProperties(UploadField.SNo.toString(), "", "1", "4"),
			new FieldProperties(UploadField.Marker.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Chromosome.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.MapName.toString(), UploadField.REQUIRED.toString(), "", ""),	
			new FieldProperties(UploadField.Position.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.TraitID.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Effect.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.Experiment.toString(), UploadField.REQUIRED.toString(), "", ""),
			new FieldProperties(UploadField.HighValueAllele.toString(), "", "", ""),	
			//new FieldProperties(UploadField.Score(e.g.,LOD (or) -log10 (p)).toString(), "", "", ""),		
			new FieldProperties(UploadField.R2.toString(), UploadField.REQUIRED.toString(), "", "")};
	
	

}
