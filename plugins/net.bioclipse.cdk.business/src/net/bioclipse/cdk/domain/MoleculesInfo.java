package net.bioclipse.cdk.domain;

public class MoleculesInfo {
	
	int noMols;
	int noMols2d;
	int noMols3d;

	public MoleculesInfo(int numMols, int num2d, int num3d) {
		noMols=numMols;
		noMols2d=num2d;
		noMols3d=num3d;
	}
	
	public int getNoMols() {
		return noMols;
	}
	public void setNoMols(int noMols) {
		this.noMols = noMols;
	}
	public int getNoMols2d() {
		return noMols2d;
	}
	public void setNoMols2d(int noMols2d) {
		this.noMols2d = noMols2d;
	}
	public int getNoMols3d() {
		return noMols3d;
	}
	public void setNoMols3d(int noMols3d) {
		this.noMols3d = noMols3d;
	}
	

}
