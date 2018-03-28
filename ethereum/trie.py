
# link: https://github.com/ethereum/wiki/wiki/Patricia-Tree

def compact_encode(hexarray):
    # whether the last element is 16, if it is 16, then it is treated as an extension node.
    term = 1 if hexarray[-1] == 16 else 0 
    # if last element is 16 then hex array is sliced that the last element is dropped
    # hexarray[:-1] get all but last element
    if term: hexarray = hexarray[:-1]
    # if odd, oddlen = 1, else 0
    oddlen = len(hexarray) % 2
    # so term is 1 for extension node type and term is 0 for terminating node type
    # flag is one nibble, 4 bits
    flags = 2 * term + oddlen
    if oddlen:
        # concat the array. insert flags in the beginning of haxarray. so the length is now even
        hexarray = [flags] + hexarray
    else:
        hexarray = [flags] + [0] + hexarray
    # hexarray now has an even length whose first nibble is the flags.
    o = ''
    # range([start], stop[, step])
    # stop: Generate numbers up to, but not including this number.
    # 0, 2, 4 .. len(hexarray) - 2
    for i in range(0,len(hexarray),2):
        # chr(i) - Return a string of one character whose ASCII code is the integer i
        # return a character that correpond to this integer
        # temp is of base 0 - (16 * 16 - 1 = 255)
        temp = chr(16 * hexarray[i] + hexarray[i+1])
        o += temp
    return o

# Here is the extended code for getting a node in the Merkle Patricia trie
def get_helper(node,path):
    # end of traversal
    if path == []: return node
    # null node
    if node = '': return ''

    # When one node is referenced inside another node, what is included is H(rlp.encode(x)),
    # where H(x) = sha3(x) if len(x) >= 32 else x and rlp.encode is the RLP encoding function.
    curnode = rlp.decode(node if len(node) < 32 else db.get(node))

    if len(curnode) == 2:
        # extenstion/leaf node
        # k2 is the path. it is in byte format
        (k2, v2) = curnode
        k2 = compact_decode(k2)
        if k2 == path[:len(k2)]:
            # path[len(k2):]
            # get path elemtent starting from len(k2). so the path is shortened
            return get(v2, path[len(k2):])
        else:
            # node not found
            return ''
    elif len(curnode) == 17:
        # branch node
        # curnode[path[0]] is the hash value so that db.get(hashVal) returns the node
        return get_helper(curnode[path[0]],path[1:])

# get the node corresponding to path starting from node
# the return result is the node if found, or '' if not found
def get(node,path):
    # convert path to be of the the format of hex. 4 bits in size. (0 - 15)
    # it is orignally in the format of byte. 32 bits in size. (0 - 256)
    path2 = []
    #0, 1, ... len(path - 1)
    for i in range(len(path)):
        # ord('x') Given a string of length one, return an integer 
        # representing the value of the byte when the argument is an 8-bit string
        # int() return an integer object

        # take the upper 4 bits
        path2.push(int(ord(path[i]) / 16))
        # take the lower 4 bits
        path2.push(ord(path[i]) % 16)
    #path2 is an array of hex numbers (from 0 to 15 )
    path2.push(16)
    return get_helper(node,path2)


# extension node
# rootHash: [ <16>, hashA ]
# 1 is the flag. extension node with path of odd length
# it correspond to path element 6, and the key is hashA.

# branch node
# hashA:    [ <>, <>, <>, <>, hashB, <>, <>, <>, hashC, <>, <>, <>, <>, <>, <>, <>, <> ]
# hashB is in the position of 4, hashC is in the position of 8

# leaf node
# hashC:    [ <20 6f 72 73 65>, 'stallion' ]
# 2 is the flg. it is a terminating leaf node with path of even length
# 0 in 20 is the padding nibble
# path is 6f 72 73 65. the value is 'stallion'

# extension node
# hashB:    [ <00 6f>, hashD ]
# 0 is the flag. it is an extension node with path of even length.
# the second 0 in 00 is the padding nibble
# corresponding path is  6 -> f. its value is a key. hashD

# branch node
# hashD:    [ <>, <>, <>, <>, <>, <>, hashE, <>, <>, <>, <>, <>, <>, <>, <>, <>, 'verb' ]
# hashE is in the position of 6. this node also has the value 'verb'

# extension node
# hashE:    [ <17>, hashF ]
# 1 is the flag. extension node with path of odd length
# it correspond to path element 7, and the key is hashF.

# branch node
# hashF:    [ <>, <>, <>, <>, <>, <>, hashG, <>, <>, <>, <>, <>, <>, <>, <>, <>, 'puppy' ]
# hashE is in the position of 6. this node also has the value 'puppy'

# leaf node
# hashG:    [ <35>, 'coin' ]
# 1 is the flag. leaf node with path of odd length
# it correspond to path element 5, and the value is 'coin'

