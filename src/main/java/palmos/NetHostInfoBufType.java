/* Source Safe Version information
 * $Header: /cvsroot/jump/jump/palmos/NetHostInfoBufType.java,v 1.1 2002/03/03 19:24:37 kleberhoff Exp $
 * $Modtime: 1/27/02 4:50p $
 * $NoKeywords: $
 */

package palmos;

/**
	* The NetHostInfoBufType struct contains information about a
	* host. The NetHostInfoType struct, which maps to the hostent
	* struct, points to fields in this struct for its information.
	*/
public class NetHostInfoBufType {
	/** A NetHostInfoType struct, which maps to the
		*  Berkeley UNIX sockets hostent structure. */
	public NetHostInfoType hostInfo;
	/** Official host name.  */
	public String		name;
	/** An array of aliases for the host name.  */
	public CharPtrArray aliasList;		// +1 for 0 termination.
	/**   */
	public CharPtrArray	aliases;
	/** An array of pointers to 32-bit IP addresses in
		*  host byte order.  */
	public int[]		addressList;
	/**   */
	public int			address;
}
	
