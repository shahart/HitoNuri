/* Source Safe Version information
 * $Header: /cvsroot/jump/jump/palmos/NetHostInfoType.java,v 1.1 2002/03/03 19:24:37 kleberhoff Exp $
 * $Modtime: 1/27/02 4:50p $
 * $NoKeywords: $
 */

package palmos;

/**
	* The NetHostInfoType structure maps to the Berkeley UNIX
	* sockets hostent structure.
	*/
public class NetHostInfoType {
	/** Official host name */
	public StringBuffer nameP;
	/** An array of aliases for the host name */
	public CharPtrArray nameAliasesP;
	/** The type of the return addresses. See NetSocketAddrEnum */
	public short		addrType;
	/** The length in bytes of the return addresses */
	public short		addreLen;
	/** An array of pointers to addresses in host byte order */
	public byte[][] 	addrListP;
}
	
