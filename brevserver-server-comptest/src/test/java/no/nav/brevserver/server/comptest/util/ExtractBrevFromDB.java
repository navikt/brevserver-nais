package no.nav.brevserver.server.comptest.util;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.comptest.AbstractComponentTest;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;


public class ExtractBrevFromDB extends AbstractComponentTest {

	
	@Autowired
	protected JdbcTemplate jdbcTemplate;
	
	
	private final String PDF_FOLDER = "C:/Temp/PDF";
	
	
	@Test
	public void extractBrev(){
		
			
		String sql = "SELECT * FROM BS475Q.T_BREVLAGER5 where systemid='OB11' and TIMESTAMP > {ts '2011-10-25 14:05:29'} fetch first 10 rows only;";
		List<BrevVO> brevliste = jdbcTemplate.query(sql, 
				new RowMapper<BrevVO>() {

					public BrevVO mapRow(ResultSet rs, int rowNum) throws SQLException {
						BrevVO brev = new BrevVO();
						brev.setBrevdata(rs.getBytes("BREVDATA"));
						brev.setBrevreferanse(rs.getString("BREVREFERANSE"));
						brev.setContentType(rs.getString("CONTENTTYPE"));
						brev.setLagerStatus(rs.getString("STATUS"));
						brev.setSystemID(rs.getString("SYSTEMID"));
						return brev;
					}
					
		});
		
		
		
		
		for(BrevVO entry : brevliste){
			File pdfFile = new File(PDF_FOLDER+"/"+entry.getBrevreferanse()+".pdf");
			System.out.println("Lagrer "+pdfFile.getName()+"...");
			
			
			try {
				FileUtils.writeByteArrayToFile(pdfFile, entry.getBrevdata());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	
	
}
