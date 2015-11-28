import java.util.List;


public class CosineSimilarity {

	/**
	 * 
	 * @param wrapperObjectList
	 * @param flag
	 * @return
	 */
	double normalizeComponent(List<QueryWrapper>wrapperObjectList,int flag){
		double normalizedRes=0;
		if(flag==0){
			for(QueryWrapper q:wrapperObjectList){
				normalizedRes+=Math.pow(q.W1, 2);
			}
		}else if(flag==1){
			for(QueryWrapper q:wrapperObjectList){
				normalizedRes+=Math.pow(q.W2, 2);
			}
		}
		normalizedRes=Math.sqrt(normalizedRes);
		return normalizedRes;
		
	}

	public double computeCosineSimilarity(List<QueryWrapper> queryInfoList,
			List<QueryWrapper> docInfoList, double normalizedQuery,
			double normalizedDocument, int flag) {
		double cosineRes=0;
		if(normalizedQuery*normalizedDocument==0)
			 return 0;
		if(flag==0){
			int i=0;
			double val=0;
			for(;i<queryInfoList.size();i++){
				QueryWrapper qWarpper=queryInfoList.get(i);
				double W1Q=qWarpper.W1;
				double W1Doc=docInfoList.get(i).W1;
				val+=W1Q*W1Doc;
			}
			cosineRes=val/(normalizedQuery*normalizedDocument);
		}else{
			int i=0;
			double val=0;
			for(;i<queryInfoList.size();i++){
				QueryWrapper qWarpper=queryInfoList.get(i);
				double W2Q=qWarpper.W2;
				double W2Doc=docInfoList.get(i).W2;
				val+=W2Q*W2Doc;
			}
			cosineRes=val/(normalizedQuery*normalizedDocument);
		}
		return cosineRes;
	}
	
	
}
